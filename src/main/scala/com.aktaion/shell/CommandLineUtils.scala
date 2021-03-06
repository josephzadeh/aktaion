package com.aktaion.shell

import java.io._
import java.util.zip.GZIPInputStream
import com.aktaion.parser._
import scala.io.{BufferedSource, Source}

object CommandLineUtils {

  /**
    * Used to run the command line Bro -r command against a input PCAP
    *
    * @param fileName input PCAP
    * @return output is the fileName of the bro file extracted from the PCAP
    */
  def extractBroFilesFromPcap(fileName: String): String  = {
    val broLogic = BroCommandLineLogic.generateBroFiles(fileName)
    if (broLogic == true) {
      val broPath = findFilePathRelativeToJar()
      //file is generated in same directory as the jar
      val broHttpFile: String = broPath + "/http.log"
      System.out.println(" Bro HTTP FilePath" + broPath)

      return broHttpFile
    }
    else ""
  }

  /**
    *
    * @return
    */
  def findFilePathRelativeToJar(): String = {
    val jarPath: File = new File(classOf[UserInteractionLogic].getProtectionDomain.getCodeSource.getLocation.getPath)
    val absolutePath: String = jarPath.getParentFile.getAbsolutePath
    return absolutePath
  }

  /**
    * Only will work on unix type paths
    *
    * @return
    */
  def tryToFindPathToDataInSourceCode(numOfSubDirs: Int = 4): String = {
    val pathStr = findFilePathRelativeToJar()
    val splitStr = pathStr.split("/").take(numOfSubDirs).mkString("/")
    val dataPath = splitStr + "/data/"
    return dataPath
  }

  /**
    *
    * @param input
    * @return
    */
  def checkBroSortedLowToHigh(input: Seq[BroHttpLogEvent]): Seq[BroHttpLogEvent] = {
    val firstTime = input.head.tsDouble
    val reverseData = input.reverse
    val lastTime = reverseData.head.tsDouble
    if (firstTime < lastTime) return input else return reverseData
  }

  /**
    *
    * @param input
    * @return
    */
  def checkProxySortedLowToHigh(input: Seq[GenericProxyLogEvent]): Seq[GenericProxyLogEvent] = {
    val firstTime = input.head.tsJavaTime.getTime
    val reverseData = input.reverse
    val lastTime = reverseData.head.tsJavaTime.getTime
    if (firstTime < lastTime) return input else return reverseData
  }

  def checkTimeSortedLowToHigh(input: Seq[NormalizedLogEvent]): Seq[NormalizedLogEvent] = {
    val firstTime = input.head.tsJavaTime.getTime
    val reverseData = input.reverse
    val lastTime = reverseData.head.tsJavaTime.getTime
    if (firstTime < lastTime) return input else return reverseData
  }

  /**
    *
    * @param fileName
    * @return
    */
  def getFileFromFileSystemPath(fileName: String): Array[String] = {
    scala.io.Source.fromFile(fileName).getLines().toArray
  }

  /**
    *
    * @param array
    */
  def debugBroArray(array: Array[String]) = {
    for (logLine <- array) {
      println(logLine)
      val parsedLine = BroHttpParser.tokenizeData(logLine)
      println(parsedLine)
    }
  }


  /**
    * U
    * @param readDirectory input read path
    * @param writeFile     ouput filename
    * @param format        "pcap" or "gzip"
    */
  def extractPcapDataFromDirectory(readDirectory: String,
                                   writeFile: String,
                                   format: String) = {
    val fileIterator = GetFileTree(new File(readDirectory)).filter(_.getName.endsWith(format)).toIterator

    for (file <- fileIterator) {
      val fileName = file.toString.split("/").last.dropRight(3) + format
      println(fileName)
      val directoryName = file.toString.split("/").reverse.tail.reverse.mkString("/") + "/"
      println(directoryName)
      val totalStr = directoryName + fileName
      println(totalStr)
      val fw = new FileWriter(totalStr, true)
      println("Crawling " + file + " for data...")

      var rawFile: BufferedSource = null
      if (format == "gzip") {
        rawFile = Source.fromInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))))
      } else {
        rawFile = Source.fromInputStream(new BufferedInputStream(new FileInputStream(file)))
      }

      while (rawFile.hasNext) {
        val line = rawFile.next
        fw.write(line)

      }
      fw.close()
    }
  }



  /**
    * Recursively walk a directory and get a list of files
    *
    * @param f
    * @return
    */
  def GetFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(GetFileTree)
    else Stream.empty)



  def printSplashScreen = {


        System.out.println("""          _____                    _____                _____                    _____                    _____                   _______                   _____ """)
        System.out.println("""         /\    \                  /\    \              /\    \                  /\    \                  /\    \                 /::\    \                 /\    \ """)
        System.out.println("""        /::\    \                /::\____\            /::\    \                /::\    \                /::\    \               /::::\    \               /::\___ \ """)
        System.out.println("""       /::::\    \              /:::/    /            \:::\    \              /::::\    \               \:::\    \             /::::::\    \             /::::|   | """)
        System.out.println("""      /::::::\    \            /:::/    /              \:::\    \            /::::::\    \               \:::\    \           /::::::::\    \           /:::::|   | """)
        System.out.println("""     /:::/\:::\    \          /:::/    /                \:::\    \          /:::/\:::\    \               \:::\    \         /:::/~~\:::\    \         /::::::|   | """)
        System.out.println("""    /:::/__\:::\    \        /:::/____/                  \:::\    \        /:::/__\:::\    \               \:::\    \       /:::/    \:::\    \       /:::/|::|   | """)
        System.out.println("""   /::::\   \:::\    \      /::::\    \                  /::::\    \      /::::\   \:::\    \              /::::\    \     /:::/    / \:::\    \     /:::/ |::|   | """)
        System.out.println("""  /::::::\   \:::\    \    /::::::\____\________        /::::::\    \    /::::::\   \:::\    \    ____    /::::::\    \   /:::/____/   \:::\____\   /:::/  |::|   | _____ """)
        System.out.println(""" /:::/\:::\   \:::\    \  /:::/\:::::::::::\    \      /:::/\:::\    \  /:::/\:::\   \:::\    \  /\   \  /:::/\:::\    \ |:::|    |     |:::|    | /:::/   |::|   |/\    \ """)
        System.out.println("""/:::/  \:::\   \:::\____\/:::/  |:::::::::::\____\    /:::/  \:::\____\/:::/  \:::\   \:::\____\/::\   \/:::/  \:::\____\|:::|____|     |:::|    |/:: /    |::|   /::\___ \ """)
        System.out.println("""\::/    \:::\  /:::/    /\::/   |::|~~~|~~~~~        /:::/    \::/    /\::/    \:::\  /:::/    /\:::\  /:::/    \::/    / \:::\    \   /:::/    / \::/    /|::|  /:::/    / """)
        System.out.println(""" \/____/ \:::\/:::/    /  \/____|::|   |            /:::/    / \/____/  \/____/ \:::\/:::/    /  \:::\/:::/    / \/____/   \:::\    \ /:::/    /   \/____/ |::| /:::/    / """)
        System.out.println("""          \::::::/    /         |::|   |           /:::/    /                    \::::::/    /    \::::::/    /             \:::\    /:::/    /            |::|/:::/    / """)
        System.out.println("""           \::::/    /          |::|   |          /:::/    /                      \::::/    /      \::::/____/               \:::\__/:::/    /             |::::::/    / """)
        System.out.println("""           /:::/    /           |::|   |          \::/    /                       /:::/    /        \:::\    \                \::::::::/    /              |:::::/    / """)
        System.out.println("""          /:::/    /            |::|   |           \/____/                       /:::/    /          \:::\    \                \::::::/    /               |::::/    / """)
        System.out.println("""         /:::/    /             |::|   |                                        /:::/    /            \:::\    \                \::::/    /                /:::/    / """)
        System.out.println("""        /:::/    /              \::|   |                                       /:::/    /              \:::\____\                \::/____/                /:::/    / """)
        System.out.println("""        \::/    /                \:|   |                                       \::/    /                \::/    /                 ~~                      \::/    / """)
        System.out.println("""         \/____/                  \|___|                                        \/____/                  \/____/                                           \/____/ """)
        System.out.print("")
        System.out.print("")
        System.out.println("Welcome to the Aktaion Scoring Engine. The executable is based off a machine learning tool and data associated with: https://github.com/jzadeh/aktaion")
  

  }


}

