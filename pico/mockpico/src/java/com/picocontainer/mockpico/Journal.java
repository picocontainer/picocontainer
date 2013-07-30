package com.picocontainer.mockpico;

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
