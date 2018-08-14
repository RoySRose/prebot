package prebot.macro.util;

public class MutableInt {
    int value; // note that we start at 1 since we're counting

    public void increment () { ++value;      }
    public int  get ()       { return value; }
    public void increment (int num) { value += num;      }

    public MutableInt() {
        this.value = 1;
    }
    public MutableInt(int value) {
        this.value = value;
    }
}
