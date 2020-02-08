public class Album implements Comparable<Album> {
    private String name; //Строка не может быть пустой, Поле не может быть null
    private int length; //Значение поля должно быть больше 0

    public Album(String name, int length) {
        this.name = name;
        this.length = length;
    }

    @Override
    public String toString() {
        return "Album{" +
                "name='" + name + '\'' +
                ", length=" + length +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public int compareTo(Album o) {
        int result = this.name.compareTo(o.name);
        if (result == 0){
            result = this.length-o.length;
        }
        return result;
    }
}
