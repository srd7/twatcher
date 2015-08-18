package twatcher.logics

import twatcher.exceptions.InsertZipTweetException
import twatcher.models.{Tweet, Tweets}
import twatcher.globals.db

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.io.Source

import java.io.{Closeable, File, FileInputStream, FileOutputStream}
import java.util.zip.ZipInputStream

import scalaz._
import scalaz.Scalaz._

import com.github.tototoshi.csv._

object FileLogic {

  private[this] def using[T <: Closeable, U](closable: T)(f: T => U): Throwable \/ U =
    try {
      f(closable).right
    } catch {
      case e: Throwable => e.left
    }finally {
      closable.close()
    }

  /**
   * Find file of certain name from zip file and store it to Array[Byte]
   */
  def unzip(zip: File, targetName: String): Throwable \/ Seq[Array[Byte]] = {
    // Read from zip file and store to Array[Byte]
    using(new ZipInputStream(new FileInputStream(zip))){ zis =>
      Iterator.continually(zis.getNextEntry()).takeWhile(_ != null).withFilter(ze =>
        !ze.isDirectory && (ze.getName == targetName || ze.getName.endsWith(s"/${targetName}"))
      ).map { _ =>
        val buf = ArrayBuffer.empty[Byte]
        // Write to ArrayBuffer[Byte]
        Iterator.continually(zis.read()).takeWhile(_ != -1).foreach(buf += _.toByte)
        buf.toArray
      }.toSeq
    }
  }

  /**
   * Read zip and insert tweets into DB
   */
  def insertTweetZip(zip: File): Future[Unit] =  {
    val tweets: Throwable \/ List[Tweet] = for {
      userDetailBytes <- unzip(zip, "user_details.js")
      userDetailStr   <- userDetailBytes.headOption.map(ba => new String(ba, "UTF-8")) \/>
        new InsertZipTweetException("user_details.js load error")
      userId          <- extractUserId(userDetailStr)
      tweetsBytes     <- unzip(zip, "tweets.csv")
      tweetsReader    <- tweetsBytes.headOption.map(ba => reader(ba)) \/>
        new InsertZipTweetException("tweets.csv load error")
      csv             =  CSVReader.open(tweetsReader)
      _               =  csv.readNext() // First line is header not data
      tweets          =  csv.all().map(line => Tweet(userId, line.head.toLong))
    } yield tweets
    // } yield null

    tweets.fold(e => { println(e); Future.failed(e)}, tweets => db.run(Tweets.insertAll(tweets)))
  }

  /**
   * WORKAROUND
   * scala-csv 1.3.0 solves this.
   * scala-csv 1.2.2 cannot parse scala.io.Source
   */
  private[this] def reader(bytes: Array[Byte]): java.io.Reader = {
    import java.io.{BufferedReader, ByteArrayInputStream, InputStreamReader}
    val is = new ByteArrayInputStream(bytes)
    new BufferedReader(new InputStreamReader(is))
  }

  private[this] def extractUserId(str: String): Throwable \/ Long = {
    val regex = """"id" : "(\d+)",""".r
    regex.findAllIn(str).matchData.map(_.group(1)).toList.headOption match {
      case Some(id) => id.toLong.right
      case _        => (new InsertZipTweetException("user_details.js parse error")).left
    }
  }
}
