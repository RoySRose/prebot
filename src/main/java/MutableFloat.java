

public class MutableFloat {
	
	float value; // note that we start at 1 since we're counting

    public void increment () { ++value;      }
    public float get ()       { return value; }
    public void increment (double num) { value += num;      }

    public MutableFloat() {
        this.value = 1;
    }
    public MutableFloat(float value) {
        this.value = value;
    }
    @Override
    public String toString(){
		return String.valueOf(value);
    }
}
