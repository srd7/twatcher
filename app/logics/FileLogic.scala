package twatcher.logics

import scala.collection.mutable.ArrayBuffer

import java.io.{Closeable, File, FileInputStream, FileOutputStream}
import java.util.zip.ZipInputStream

object FileLogic {

  private[this] def using[T <: Closeable, U](closable: T)(f: T => U): Either[Throwable, U] =
    try {
      Right(f(closable))
    } catch {
      case e: Throwable => Left(e)
    }finally {
      closable.close()
    }

  /**
   * Find file of certain name from zip file and store it to Array[Byte]
   */
  def unzip(zip: File, targetName: String): Either[Throwable, Seq[Array[Byte]]] = {
    // Read from zip file and store to Array[Byte]
    using(new ZipInputStream(new FileInputStream(zip))){ zis =>
      Iterator.continually(zis.getNextEntry()).takeWhile(_ != null).withFilter(ze =>
        !ze.isDirectory && ze.getName.endsWith(s"/${targetName}")
      ).map { _ =>
        val buf = ArrayBuffer.empty[Byte]
        // Write to ArrayBuffer[Byte]
        Iterator.continually(zis.read()).takeWhile(_ != -1).foreach(buf += _.toByte)
        buf.toArray
      }.toSeq
    }
  }
}
