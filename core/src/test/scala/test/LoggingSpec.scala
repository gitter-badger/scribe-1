package test

import com.outr.scribe.formatter.Formatter
import com.outr.scribe.writer.Writer
import com.outr.scribe._
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable.ListBuffer

/**
 * @author Matt Hicks <matt@outr.com>
 */
class LoggingSpec extends WordSpec with Matchers with Logging {
  updateLogger { l =>
    l.copy(parent = None)
  }
  val handler = LogHandler(level = Level.Debug, writer = TestingWriter)
  logger.addHandler(handler)

  "Logging" should {
    "have no logged entries yet" in {
      TestingWriter.records.length should be(0)
    }
    "log a single entry after info log" in {
      logger.info("Info Log")
      TestingWriter.records.length should be(1)
    }
    "log a second entry after debug log" in {
      logger.debug("Debug Log")
      TestingWriter.records.length should be(2)
    }
    "ignore the third entry after reconfiguring without debug logging" in {
      logger.removeHandler(handler)
      logger.addHandler(LogHandler(level = Level.Info, writer = TestingWriter))
      logger.debug("Debug Log 2")
      TestingWriter.records.length should be(2)
    }
    "boost the this logging instance" in {
      updateLogger(_.copy(multiplier = 2.0))
      logger.debug("Debug Log 3")
      TestingWriter.records.length should be(3)
    }
    "not increment when logging to the root logger" in {
      Logger.Root.error("Error Log 1")
      TestingWriter.records.length should be(3)
    }
    "write a detailed log message" in {
      val lineNumber = 65
      TestingWriter.clear()
      LoggingTestObject.testLogger()
      TestingWriter.records.length should be(1)
      TestingWriter.records.head.methodName should be(Some("testLogger"))
      TestingWriter.records.head.lineNumber should be(Some(lineNumber))
    }
  }
}

object LoggingTestObject extends Logging {
  updateLogger { l =>
    l.copy(parent = None, includeTrace = true)
  }
  logger.addHandler(LogHandler(Level.Debug, writer = TestingWriter))

  def testLogger(): Unit = {
    logger.info("This is a test!")
  }
}

object TestingWriter extends Writer {
  val records = ListBuffer.empty[LogRecord]

  def write(record: LogRecord, formatter: Formatter): Unit = records += record

  def clear(): Unit = records.clear()
}