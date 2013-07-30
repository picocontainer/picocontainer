/**
 * Copyright (c) 2010 ThoughtWorks
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.thoughtworks.mockpico;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

public class Journal {

    private final List<Object> things = new ArrayList<Object>();

    public void append(Object thing) {
        things.add(thing);
    }

    static class Arg {
        private final int i;
        private final Class<?> type;
        private final Object o;

        public Arg(int i, Class<?> type, Object o) {
            this.i = i;
            this.type = type;
            this.o = o;
        }

        public String toString() {
            return "  arg[" + i + "] type:" + type + ", with: " + o.toString() + "\n";
        }

    }

    static class Field {
        private final Member member;
        private final Object arg;

        public Field(Member member, Object arg) {
            this.member = member;
            this.arg = arg;
        }
        @Override
        public String toString() {
            return "Field being injected: '" + member.getName() + "' with: " + arg + "\n";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Object thing : things) {
            sb.append(thing.toString());
        }
        return sb.toString();
    }
}
