/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.panson.common.web.exception;

/**
 * A generic exception to handle illegal operations
 */
public class IllegalException extends RuntimeException {

    private static final long serialVersionUID = 8247610319171014183L;

    public IllegalException() {
        super();
    }

    public IllegalException(String message) {
        super(message);
    }

    public IllegalException(Throwable e) {
        super(e.getMessage(), e);
    }

    public IllegalException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public IllegalException(String message, Throwable throwable, boolean enableSuppression, boolean writableStackTrace) {
        super(message, throwable, enableSuppression, writableStackTrace);
    }

}
