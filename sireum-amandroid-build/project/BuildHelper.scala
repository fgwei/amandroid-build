import sbt._
import scala.collection.mutable._
import java.util.Properties
import java.io.FileOutputStream
import java.util.TreeMap
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.io.BufferedReader
import java.io.FileReader
import java.security.MessageDigest
import java.io.BufferedInputStream
import java.security.DigestInputStream
import java.io.FileInputStream
import java.io.LineNumberReader

object BuildHelper {

  val env = {
    import scala.collection.JavaConversions._
    val env = System.getenv
    val envl = new Array[String](env.size)
    var i = 0
    for (e <- env) {
      envl(i) = e._1 + "=" + e._2
      i = i + 1
    }
    envl
  }

  def relativize(baseDir : File)(f : File) = {
    IO.relativize(baseDir, f) match {
      case Some(s) => s
      case _       => sys.error("Error: " + f.getAbsolutePath)
    }
  }

  def timeStamp = {
    val dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss")
    val date = new Date
    dateFormat.format(date)
  }

  def buildAmandroid(amandroidDir : File, projectInfoMap : Map[String, ProjectInfo],
                 dirs : scala.collection.Seq[String]) {

    println("Sireum dir: " + amandroidDir.getAbsolutePath)

    val uds =
      dirs.toList match {
        case List("-h") =>
          println("Usage: [<amandroid-build-dir>]")
          return
        case amandroidBuildDir :: Nil => AmandroidDirs(new File(amandroidBuildDir))
        case Nil                     => AmandroidDirs(amandroidDir / "build")
      }

    val checksums = newProperties

    def relBase = relativize(uds.baseDir) _

    def addChecksum(f: File) {
      checksums.put(relBase(f), getChecksum(f))
    }

    val fileMap = Map[File, File]()

    withTempDir(amandroidDir, "temp") { tempDir =>
      for (pi <- projectInfoMap.values) {
        for (f <- pi.libFiles)
          if (fudgeJar(f, tempDir)) {
            val fLib = uds.libDir / f.getName
            IO.copyFile(f, fLib, true)
            fileMap(f) = fLib
            addChecksum(fLib)
          }

        for (f <- pi.srcFiles)
          if (fudgeJar(f, tempDir)) {
            val fSrc = uds.srcDir / f.getName.replace(".jar", ".zip")
            IO.copyFile(f, fSrc, true)
            fileMap(f) = fSrc
            addChecksum(fSrc)
          }

        for (f <- pi.licensesFiles) {
          val fLicense = uds.licensesDir / f.getName
          IO.copyFile(f, fLicense, true)
          fileMap(f) = fLicense
          addChecksum(fLicense)
        }
      }
    }

    // copy stash
    val stashDir = amandroidDir / "stash"
    if(stashDir.exists()) {
      for(f <- stashDir.listFiles()) {
        val fname = f.getName
        if(f.isDirectory())
          IO.copyDirectory(f, uds.baseDir / fname, true, true)
        else IO.copyFile(f, uds.baseDir/ fname, true)
      }
    }
    
    // copy scripts
    val launcherDir = amandroidDir / "scripts"
    val script = launcherDir / "amandroid"
    val winScript = launcherDir / "amandroid.bat"
    val fScript = uds.baseDir / script.getName
    val fWinScript = uds.baseDir / winScript.getName
    val scriptStr = buildScript(script, amandroidDir)
    val winScriptStr = buildScript(winScript, amandroidDir)
    IO.write(fScript, scriptStr)
    fScript.setExecutable(true)
    fileMap(script) = fScript
    addChecksum(fScript)
    IO.write(fWinScript, winScriptStr)
    fWinScript.setExecutable(true)
    fileMap(winScript) = fWinScript
    addChecksum(fWinScript)

    writeProperties(uds.baseDir / "checksums.properties", checksums)
    writeBuildStamp(uds.baseDir)
  }
  
  def readFileContent(file: File): String = {
    val fr = new FileReader(file)
    try{
      val lnr = new LineNumberReader(fr)
      var sb = new StringBuilder
      var lineText = lnr.readLine
      while (lineText != null) {
        sb.append(lineText)
        sb.append('\n')
        lineText = lnr.readLine
      }
      sb.toString
    } finally fr.close
  }
  def buildScript(script: File, amandroidDir: File): String = {
    val result: StringBuilder = new StringBuilder
    result.append(readFileContent(script))
    result.append("\n")
    val amandroidMainScala = amandroidDir / "/project/AmandroidMain.scala"
    result.append(readFileContent(amandroidMainScala))
    result.toString().trim()
  }
  
  def writeBuildStamp(baseDir : File) {
    val pw = new PrintWriter(new FileWriter(baseDir / AmandroidBuild.BUILD_FILENAME))
    pw.println(timeStamp)
    pw.close
  }

  def newProperties : Map[String, String] = {
    import scala.collection.JavaConversions._

    new TreeMap[String, String]()
  }

  def writeProperties(f : File, ms : Map[String, String]*) {
    val pw = new PrintWriter(new FileWriter(f))
    for (m <- ms)
      for (e <- m) {
        pw.print(e._1.replace(" ", "\\ "))
        pw.print("=")
        pw.println(e._2)
      }
    pw.close
  }

  case class AmandroidDirs(baseDir : File) {
    val libDir = baseDir / "lib"
    val srcDir = baseDir / "src"
    val licensesDir = baseDir / "licenses"
    val platformDir = baseDir / "platform"
    IO.delete(baseDir)
    baseDir.mkdirs()
    IO.createDirectory(baseDir)
    IO.createDirectory(libDir)
    IO.createDirectory(srcDir)
    IO.createDirectory(licensesDir)
    IO.createDirectory(platformDir)
  }

  def withTempDir[T](baseDir : File, tempName : String)(f : File => T) : T = {
    val tempDir = baseDir / tempName
    val r = f(tempDir)
    IO.delete(tempDir)
    r
  }

  def getAllFilesToZip(anchor : File, dir : File, acc : Map[String, File]) {
    for (f <- dir.listFiles) {
      if (f.isDirectory)
        getAllFilesToZip(anchor, f, acc)
      else
        acc(IO.relativize(anchor, f).get) = f
    }
  }

  def fudgeJar(f : File, tempDir : File) : Boolean = {
    if (!f.getName.startsWith("sireum-")) return true
    import scala.collection.JavaConversions._

    withTempDir(tempDir, f.getName.substring(0, f.getName.length - 4)) { fTempDir =>
      IO.unzip(f, fTempDir)
      val metaDir = fTempDir / "META-INF"
      if (metaDir.list == null || metaDir.list.length == 1)
        IO.delete(metaDir)
      else
        IO.delete(metaDir / "MANIFEST.MF")
      val filesToZip = new java.util.TreeMap[String, File]()
      getAllFilesToZip(fTempDir, fTempDir, filesToZip)
      if (filesToZip.isEmpty) {
        println("Empty jar: " + f.getAbsoluteFile)
        false
      } else {
        IO.delete(f)
        MyIO.zip(filesToZip.toSeq.map { p => (p._2, p._1) }, f, false)
        true
      }
    }
  }

  def getChecksum(file : File) = {
    val md = MessageDigest.getInstance("MD5")

    val is = new BufferedInputStream(new FileInputStream(file))
    try {
      val dis = new DigestInputStream(is, md)
      while (dis.read != -1) {}
    } finally is.close

    val digest = md.digest

    val result = new StringBuilder
    for (i <- 0 until digest.length) {
      val s = Integer.toString((digest(i) & 0xff), 16)
      if (s.length == 1) result.append('0')
      result.append(s)
    }

    result.toString
  }

  def readLine(file : File) = {
    val r = new BufferedReader(new FileReader(file))
    val result = r.readLine.trim
    r.close
    result
  }
}
