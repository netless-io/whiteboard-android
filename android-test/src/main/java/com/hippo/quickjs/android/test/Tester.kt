/*
 * Copyright 2019 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.quickjs.android.test

import android.content.Context
import com.getkeepsafe.relinker.ReLinker
import net.lingala.zip4j.core.ZipFile
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import kotlin.concurrent.thread

class Tester(
  private val context: Context
) {

  companion object {
    const val MAX_MESSAGE_VOLUME = 1000
  }

  private val printer = MessageList(MAX_MESSAGE_VOLUME)

  private val assetsNameFile = File(context.filesDir, "testassets.name")
  private val assetsDir = File(context.filesDir, "testassets")
  private val tempFile = File(context.cacheDir, "testassets.zip")

  private var testNumber = 0

  fun registerMultiPrinter(multiPrinter: MultiPrinter) {
    printer.registerMultiPrinter(multiPrinter)
  }

  private fun ensureAssetFiles() {
    val exceptAssetsName = try {
      assetsNameFile.readText()
    } catch (e: IOException) {
      null
    }

    var actualAssetsName: String? = null
    for (asset in context.assets.list("") ?: emptyArray()) {
      if (asset.startsWith("testassets-") && asset.endsWith(".crc32")) {
        actualAssetsName = asset
      }
    }
    if (actualAssetsName == null) {
      error("Can't find test assets")
    }

    if (exceptAssetsName != actualAssetsName) {
      printer.print("Need exact assets")
      printer.print("except = $exceptAssetsName")
      printer.print("actual = $actualAssetsName")

      assetsDir.deleteRecursively()
      if (!assetsDir.mkdirs()) {
        error("Can't create test assets dir")
      }

      context.assets.open("testassets.zip").use { `in` ->
        tempFile.outputStream().use { out ->
          `in`.copyTo(out)
        }
      }

      val zipFile = ZipFile(tempFile)
      zipFile.extractAll(assetsDir.path)

      assetsNameFile.writeText(actualAssetsName)

      printer.print("All test assets are copied")
    } else {
      printer.print("All test assets are UP-TO-DATE")
    }
  }

  private fun ensureExecutable() {
    ReLinker.loadLibrary(context, "patch_test")
    ReLinker.loadLibrary(context, "qjs")
    ReLinker.loadLibrary(context, "qjsbn")
  }

  private fun runTest(name: String, executable: String, vararg parameters: String) {
    printer.print("********************************")
    printer.print("** ${++testNumber}. $name")
    printer.print("********************************")

    val code = run(executable, *parameters)

    printer.print("exit code: $code")
  }

  private fun testPatch() {
    runTest("patch test", "patch_test")
  }

  private fun runJsTest(executable: String, parameter: String, file: String) {
    val name = "$executable $parameter $file"
    runTest(name, executable, parameter, File(assetsDir, file).path)
  }

  private fun testJs() {
    runJsTest("qjs", "", "tests/test_closure.js")
    runJsTest("qjs", "", "tests/test_op.js")
    runJsTest("qjs", "", "tests/test_builtin.js")
    runJsTest("qjs", "", "tests/test_loop.js")
    runJsTest("qjs", "-m", "tests/test_std.js")
    runJsTest("qjsbn", "", "tests/test_closure.js")
    runJsTest("qjsbn", "", "tests/test_op.js")
    runJsTest("qjsbn", "", "tests/test_builtin.js")
    runJsTest("qjsbn", "", "tests/test_loop.js")
    runJsTest("qjsbn", "-m", "tests/test_std.js")
    runJsTest("qjsbn", "--qjscalc", "tests/test_bignum.js")
  }

  fun start() {
    thread {
      try {
        ensureAssetFiles()
        ensureExecutable()

        testPatch()
        testJs()
      } catch (e: Throwable) {
        e.printStackTrace()
        printer.print("Test interrupted")
        printer.print(e.message ?: e.javaClass.name)
        return@thread
      }
    }
  }

  private fun run(executable: String, vararg parameters: String): Int {
    val nativeDir = context.applicationInfo.nativeLibraryDir
    val executableFile = File(nativeDir, "lib$executable.so")
    val command = "${executableFile.path} ${parameters.joinToString(" ")}"

    val process = Runtime.getRuntime().exec(command)

    process.inputStream.reader().buffered().forEachLine { printer.print(it) }
    process.errorStream.reader().buffered().forEachLine { printer.print(it) }

    return process.waitFor()
  }

  /**
   * MessageList cache messages and dispatch message to the last registered MultiPrinter.
   */
  private class MessageList(private val volume: Int) : Printer {

    init {
      check(volume > 0) { "Invalid volume: $volume" }
    }

    private val messages = LinkedList<String>()

    @Volatile
    private var weakMultiPrinter: WeakReference<MultiPrinter>? = null

    fun registerMultiPrinter(multiPrinter: MultiPrinter) {
      synchronized(messages) {
        messages.forEach {
          multiPrinter.print(it)
        }
      }
      weakMultiPrinter = WeakReference(multiPrinter)
    }

    override fun print(message: String) {
      synchronized(message) {
        while (messages.size >= volume) {
          messages.poll()
        }
        messages.offer(message)
      }
      weakMultiPrinter?.get()?.print(message)
    }
  }
}