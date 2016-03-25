/*******************************************************************************
 * Copyright (c) 2013 - 2016 Fengguo Wei and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Detailed contributors are listed in the CONTRIBUTOR.md
 ******************************************************************************/
import sbt._
import Keys._
import sbt.complete.Parsers._
import scala.collection.mutable._
import eu.henkelmann.sbt.JUnitXmlTestsListener
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbtunidoc.Plugin._
import UnidocKeys._
import com.github.retronym.SbtOneJar
import com.typesafe.sbt.SbtAspectj._


/**
 * @author <a href="mailto:fgwei@k-state.edu">Fengguo Wei</a>
 */ 
object AmandroidBuild extends Build {
  final val BUILD_FILENAME = "BUILD"
  final val PRELUDE_DIR = "codebase/prelude/"
  final val CORE_DIR = "codebase/core/"
  final val PARSER_DIR = "codebase/parser/"
  final val JAWA_DIR = "codebase/jawa/"
  final val AMANDROID_DIR = "codebase/amandroid/"
  final val AMANDROID_BUILD_DIR = "codebase/amandroid-build/"

  import ProjectInfo._
  
  val buildAmandroid = InputKey[Unit]("build-amandroid", "Build Amandroid.")
  
  lazy val amandroid_project =
    Project(
      id = "amandroid",
      settings = amandroidSettings ++ Seq(
          buildAmandroid := {
            val args = spaceDelimited("<arg>").parsed
            BuildHelper.buildAmandroid(baseDirectory.value, projectInfoMap, args)
          }) ++ unidocSettings ++ Seq(
          unidocProjectFilter in (ScalaUnidoc, unidoc) := 
            inAnyProject 
            -- inProjects(lib)
            -- inProjects(macr)
            -- inProjects(util)
            -- inProjects(parser)
            -- inProjects(pilar)
            -- inProjects(alir)
            -- inProjects(option)
            -- inProjects(amandroidProject)
            -- inProjects(amandroidTest)
            -- inProjects(jawaTest)
          ) ++ SbtOneJar.oneJarSettings,
      base = file(".")) aggregate (
        lib, macr, util, parser,
        pilar, alir,
        option, amandroidProject,
        jawa, jawaCompiler, jawaAlir, jawaTest,
        amandroidDedex, amandroid, amandroidAlir, amandroidSecurity, amandroidSerialization, amandroidConcurrent, amandroidCli, amandroidTest, amandroidRun
        ) settings (
          name := "Amandroid")

  final val scalaVer = "2.11.7"
  
  val sireumSettings = Defaults.defaultSettings ++ Seq(
    organization := "Argus Laboratory",
    artifactName := { (config : ScalaVersion, module : ModuleID, artifact : Artifact) =>
      artifact.name + (
        artifact.classifier match {
          case Some("sources") => "-src"
          case Some("javadoc") => "-doc"
          case _               => ""
        }) + "." + artifact.extension
    },
    incOptions := incOptions.value.withNameHashing(true),
    parallelExecution in Test := false,
    scalaVersion := scalaVer,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVer,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVer,
    libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.7" % "test",
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
    testListeners <<= (target, streams).map((t, s) => Seq(new JUnitXmlTestsListener(t.getAbsolutePath)))
  )
  
  final val kamonVersion = "0.3.4"
  
  val amandroidSettings = sireumSettings ++ Seq(
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    scalacOptions in (Compile, doc) ++= Opts.doc.title("Sireum-Amandroid-Api-Doc"),
    scalacOptions in (Compile, doc) ++= Seq("-doc-root-content", baseDirectory.value+"/root-doc.txt"),
    autoAPIMappings := true,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.3.5",
      "com.typesafe.akka" %% "akka-cluster" % "2.3.5",
      "io.kamon" %% "kamon-core" % kamonVersion,
      "io.kamon" %% "kamon-statsd" % kamonVersion,
      "io.kamon" %% "kamon-log-reporter" % kamonVersion,
      "io.kamon" %% "kamon-system-metrics" % kamonVersion,
      "org.aspectj" % "aspectjweaver" % "1.8.1"
    )
  )
  
  aspectjSettings
 
  javaOptions <++= AspectjKeys.weaverOptions in Aspectj
   
  // when you call "sbt run" aspectj weaving kicks in
  fork in run := true

  lazy val lib = toSbtProject(libPI)
  lazy val macr = toSbtProject(macroPI)
  lazy val util = toSbtProject(utilPI)
  lazy val parser = toSbtProject(parserPI)
  lazy val pilar = toSbtProject(pilarPI)
  lazy val alir = toSbtProject(alirPI)
  lazy val option = toSbtProject(optionPI)
  lazy val amandroidProject = toSbtProject(amandroidProjectPI, amandroidSettings)
  lazy val jawa = toSbtProject(jawaPI)
  lazy val jawaCompiler = toSbtProject(jawaCompilerPI)
  lazy val jawaAlir = toSbtProject(jawaAlirPI)
  lazy val jawaTest = toSbtProject(jawaTestPI)
  lazy val amandroidDedex = toSbtProject(amandroidDedexPI, amandroidSettings)
  lazy val amandroid = toSbtProject(amandroidPI, amandroidSettings)
  lazy val amandroidAlir = toSbtProject(amandroidAlirPI, amandroidSettings)
  lazy val amandroidSecurity = toSbtProject(amandroidSecurityPI, amandroidSettings)
  lazy val amandroidSerialization = toSbtProject(amandroidSerializationPI, amandroidSettings)
  lazy val amandroidConcurrent = toSbtProject(amandroidConcurrentPI, amandroidSettings)
  lazy val amandroidCli = toSbtProject(amandroidCliPI, amandroidSettings)
  lazy val amandroidTest = toSbtProject(amandroidTestPI, amandroidSettings)
  lazy val amandroidRun = toSbtProject(amandroidRunPI, amandroidSettings)

  def firstExists(default : String, paths : String*) : String = {
    for (p <- paths)
      if (new File(p).exists)
        return p
    val f = new File(System.getProperty("user.home") + "/" + default)
    f.mkdirs
    val path = f.getAbsolutePath
    println("Using " + path)
    path
  }

  def toSbtProject(pi : ProjectInfo) : Project =
    Project(
      id = pi.id,
      settings = sireumSettings,
      base = pi.baseDir).
      dependsOn(pi.dependencies.map { p =>
        new ClasspathDependency(new LocalProject(p.id), None)
      } : _*).
      settings(name := pi.name)
      
  def toSbtProject(pi : ProjectInfo, mysettings : scala.collection.Seq[sbt.Def.Setting[_]]) : Project =
    Project(
      id = pi.id,
      settings = mysettings,
      base = pi.baseDir).
      dependsOn(pi.dependencies.map { p =>
        new ClasspathDependency(new LocalProject(p.id), None)
      } : _*).
      settings(name := pi.name)

  val libPI = new ProjectInfo("Sireum Lib", PRELUDE_DIR, Seq())
  val macroPI = new ProjectInfo("Sireum Macro", PRELUDE_DIR, Seq(), libPI)
  val utilPI = new ProjectInfo("Sireum Util", PRELUDE_DIR, Seq(),
    libPI)
  val parserPI = new ProjectInfo("Sireum Parser", PARSER_DIR, Seq(),
    libPI)
  val pilarPI = new ProjectInfo("Sireum Pilar", CORE_DIR, Seq(),
    libPI, utilPI, parserPI)
  val alirPI = new ProjectInfo("Sireum Alir", CORE_DIR, Seq(),
    libPI, utilPI, pilarPI)
  val optionPI = new ProjectInfo("Sireum Option", CORE_DIR, Seq(),
    macroPI, utilPI)
  val amandroidProjectPI = new ProjectInfo("Sireum Amandroid Project", AMANDROID_BUILD_DIR, Seq(),
    libPI)
  val jawaPI = new ProjectInfo("Sireum Jawa",
    JAWA_DIR, Seq(),
    libPI, utilPI, pilarPI)
  val jawaCompilerPI = new ProjectInfo("Sireum Jawa Compiler",
    JAWA_DIR, Seq(),
    libPI, utilPI, pilarPI, parserPI, alirPI, jawaPI)
  val jawaAlirPI = new ProjectInfo("Sireum Jawa Alir",
    JAWA_DIR, Seq(),
    libPI, utilPI, pilarPI, alirPI, jawaPI)
  val jawaTestPI = new ProjectInfo("Sireum Jawa Test",
    JAWA_DIR, Seq(),
    libPI, utilPI, pilarPI, alirPI, jawaPI, jawaAlirPI)
  val amandroidDedexPI = new ProjectInfo("Sireum Amandroid Dedex",
    AMANDROID_DIR, Seq("Amandroid"),
    libPI, utilPI, jawaPI)
  val amandroidPI = new ProjectInfo("Sireum Amandroid",
    AMANDROID_DIR, Seq("Amandroid"),
    libPI, utilPI, pilarPI, parserPI, alirPI, optionPI, jawaPI, jawaAlirPI, amandroidDedexPI)
  val amandroidAlirPI = new ProjectInfo("Sireum Amandroid Alir",
    AMANDROID_DIR, Seq("Amandroid"),
    libPI, utilPI, pilarPI, parserPI, alirPI, optionPI, jawaPI, jawaAlirPI, amandroidDedexPI, amandroidPI)
  val amandroidSecurityPI = new ProjectInfo("Sireum Amandroid Security",
    AMANDROID_DIR, Seq("Amandroid"),
    libPI, utilPI, pilarPI, parserPI, alirPI, optionPI, jawaPI, jawaAlirPI, amandroidDedexPI, amandroidPI, amandroidAlirPI)
  val amandroidSerializationPI = new ProjectInfo("Sireum Amandroid Serialization",
    AMANDROID_DIR, Seq("Amandroid"),
    libPI, utilPI, pilarPI, alirPI, optionPI, jawaPI, jawaAlirPI, amandroidPI, amandroidAlirPI, amandroidSecurityPI)
  val amandroidConcurrentPI = new ProjectInfo("Sireum Amandroid Concurrent",
    AMANDROID_DIR, Seq("Amandroid"),
    libPI, utilPI, pilarPI, alirPI, optionPI, jawaPI, jawaAlirPI, amandroidPI, amandroidAlirPI, amandroidSecurityPI, amandroidSerializationPI)
  val amandroidCliPI = new ProjectInfo("Sireum Amandroid Cli",
    AMANDROID_DIR, Seq("Amandroid"),
    libPI, utilPI, pilarPI, parserPI, alirPI, optionPI, jawaPI, jawaAlirPI, amandroidDedexPI, amandroidPI, amandroidAlirPI, amandroidSecurityPI, amandroidSerializationPI, amandroidConcurrentPI)
  val amandroidTestPI = new ProjectInfo("Sireum Amandroid Test",
    AMANDROID_DIR, Seq("Amandroid"),
    libPI, utilPI, pilarPI, parserPI, alirPI, optionPI, jawaPI, jawaAlirPI, amandroidDedexPI, amandroidPI,
    amandroidAlirPI, amandroidSecurityPI, amandroidSerializationPI, amandroidConcurrentPI, jawaTestPI)
  val amandroidRunPI = new ProjectInfo("Sireum Amandroid Run",
    AMANDROID_DIR, Seq("Amandroid"),
    libPI, utilPI, pilarPI, parserPI, alirPI, optionPI, jawaPI, jawaAlirPI, amandroidDedexPI, amandroidPI,
    amandroidAlirPI, amandroidSecurityPI, amandroidSerializationPI, amandroidConcurrentPI)
  lazy val projectInfoMap : Map[String, ProjectInfo] = Map(
    Seq(
      libPI,
      macroPI,
      utilPI,
      pilarPI,
      parserPI,
      alirPI,
      optionPI,
      jawaPI,
      jawaCompilerPI,
      jawaAlirPI,
      amandroidDedexPI,
      amandroidPI,
      amandroidAlirPI,
      amandroidSecurityPI,
      amandroidSerializationPI,
      amandroidConcurrentPI,
      amandroidCliPI,
      amandroidRunPI
    ).map { pi => pi.id -> pi } : _*)
}