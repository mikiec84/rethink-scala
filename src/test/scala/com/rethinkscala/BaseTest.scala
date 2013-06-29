package com.rethinkscala

import org.scalatest.{ BeforeAndAfter, FunSuite }
import com.rethinkscala.ast.Produce
import org.scalatest.exceptions.TestFailedException

/** Created by IntelliJ IDEA.
 *  User: Keyston
 *  Date: 6/18/13
 *  Time: 3:45 PM
 */

import ql2._
import com.rethinkscala.Implicits.Quick._

trait BaseTest extends BeforeAndAfter {
  self: FunSuite =>
  val host = (Option(scala.util.Properties.envOrElse("TRAVIS", "empty")) map {
    case "empty" => "172.16.2.45"
    case _       => "127.0.0.1"
  }).get
  val port = 28015
  val authKey = ""
  val version1 = new Version1(host, port)
  val version2 = new Version2(host, port, authKey = authKey)

  lazy val tableName = randomAlphanumericString(5)

  // Random generator
  val random = new scala.util.Random

  // Generate a random string of length n from the given alphabet
  def randomString(alphabet: String)(n: Int): String =
    Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(n).mkString

  // Generate a random alphabnumeric string of length n
  def randomAlphanumericString(n: Int) =
    randomString("abcdefghijklmnopqrstuvwxyz0123456789")(n)
  def useVersion = version1

  type IS = Iterable[String]
  type IA = Iterable[Any]
  implicit val connection: Connection = new Connection(useVersion)

  def assert(t: ql2.Term, tt: Term.TermType.EnumVal) {
    assert(t.`type`.get == tt)
  }

  def assert(d: Option[ql2.Datum], value: String) {
    assert(d.get.str == value)
  }

  def assert(d: ql2.Datum, value: String) {
    assert(d.str == value)
  }

  private def assert[Result](f: () => Either[RethinkError, Result], check: Result => Boolean)(implicit mf: Manifest[Result]) {
    val (condition, cause) = f() match {
      case Left(e)  => (false, e.getMessage)
      case Right(r) => (check(r), "Successful query but invalid response")
    }
    if (!condition)
      throw new TestFailedException(cause, 5)
  }
  def assert[Result](query: Produce[Result], testValue: Result)(implicit mf: Manifest[Result]) {
    assert[Result](() => query.run, {
      r: Result => r == testValue
    })

  }

  def assert(query: Produce[Boolean]) {
    assert[Boolean](query, true)
  }
  def assert[Result](query: Produce[Result], check: Result => Boolean)(implicit mf: Manifest[Result]) {
    assert[Result](() => query.run, check)

  }

  def assertAs[Result <: Document](query: Produce[Document], check: Result => Boolean)(implicit mf: Manifest[Result]) {

    assert[Result](() => query.as[Result], check)

  }

  def assert0(condition: Boolean) {
    if (!condition)
      throw new TestFailedException(5)
  }

}