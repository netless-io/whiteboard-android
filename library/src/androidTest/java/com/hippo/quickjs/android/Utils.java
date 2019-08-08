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

package com.hippo.quickjs.android;

import static org.junit.Assert.*;

public class Utils {

  public static <T extends Throwable> void assertException(Class<T> type, String message, Block block) {
    try {
      block.run();
      fail();
    } catch (Throwable e) {
      assertTrue("excepted: ${type.name}, actual: ${e.javaClass.name}", type.isInstance(e));
      assertEquals(message, e.getMessage());
    }
  }

  public interface Block {
    void run();
  }
}