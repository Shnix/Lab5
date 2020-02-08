import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Run {
    private static final Map<Integer, MusicBand> data = new HashMap<>();
    private static final Date date = new Date();
    private static File xml = null;

    public static void main(String[] args) throws IOException {
        xml = new File("D:/kek.xml");
        File newXml = new File("test.xml");
        readAndWrite(newXml);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xml);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("MusicBand");
            NodeList nodeList2 = doc.getElementsByTagName("Coordinates");
            NodeList nodeList3 = doc.getElementsByTagName("Album");
            // создадим из него список объектов MusicBand
            for (int i = 0; i < nodeList.getLength(); i++) {
                MusicBand musicBand = new MusicBand();
                getMusicBand(musicBand, nodeList.item(i));
                musicBand.setCoordinates(getCoords(nodeList2.item(i)));
                musicBand.setBestAlbum(getAlbum(nodeList3.item(i)));
                data.put(i, musicBand);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                String command = reader.readLine();
                if (command.equals("exit")) {
                    break;
                }
                start(command);
            } catch (NumberFormatException e) {
                System.out.println("Значение введено неверно");
            } catch (ArrayIndexOutOfBoundsException ex) {
                System.out.println("Вы не указали значение");
            }
        }
    }

    private static void readAndWrite(File file) throws IOException {
        FileReader fileReader = new FileReader(xml);
        file.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        int i = 0;
        while (fileReader.ready()) {
            int data = fileReader.read();
            if (i == 0) {
                i++;
                continue;
            } //пропускаем первую итерацию чтобы не записывать 3 первые байта xml
            fileOutputStream.write(data);
        }
        fileReader.close();
        fileOutputStream.close();
    }

    private static void getMusicBand(MusicBand musicBand, Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            musicBand.setName(getTagValue("name", element));
            musicBand.setNumberOfParticipants(Long.parseLong(getTagValue("numberOfParticipants", element)));
            musicBand.setSinglesCount(Long.parseLong(getTagValue("singles", element)));
            musicBand.setGenre(MusicGenre.valueOf(getTagValue("genre", element)));
        }
    }

    public static Coordinates getCoords(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            return new Coordinates(Float.parseFloat(getTagValue("x", element))
                    , Double.parseDouble(getTagValue("y", element)));
        }
        return null;
    }

    public static Album getAlbum(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            return new Album(getTagValue("albumName", element),
                    Integer.parseInt(getTagValue("length", element)));
        }
        return null;
    }

    private static String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }

    public static void start(String command) throws IOException, NumberFormatException {

        String[] line = command.split(" ");
        switch (line[0]) {
            case "help":
                printHelp();
                break;

            case "info":
                printInfo();
                break;

            case "insert":
                boolean k = true;
                int i = Integer.parseInt(line[1]);
                for (Map.Entry<Integer, MusicBand> band : data.entrySet()) {
                    if (band.getKey().equals(i)) {
                        System.out.println("Элемент с таким ключом уже существует");
                        k = false;
                    }
                }
                if (k) {
                    data.put(i, insert());
                }
                break;

            case "update":
                int j = Integer.parseInt(line[1]);
                int count = 0;
                for (Map.Entry<Integer, MusicBand> band : data.entrySet()) {
                    if (band.getValue().getId() == j) {
                        System.out.println("Введите новые данные для обновления элемента");
                        band.getValue().update();
                        System.out.println("Данные обновлены");
                    } else count++;
                }
                if (count == data.size()) {
                    System.out.println("Элемента с таким id не существует");
                }
                break;

            case "show":
                show();
                break;

            case "remove":
                if (data.get(Integer.parseInt(line[1])) == null) {
                    System.out.println("Элемента с таким ключом не существует");
                } else {
                    data.remove(Integer.parseInt(line[1]));
                    System.out.println("Элемент успешно удален");
                }
                break;

            case "clear":
                data.entrySet().removeIf(entry -> entry.getKey() != null);
                System.out.println("Все данные удалены");
                break;

            case "execute_script":
                BufferedReader reader;
                try {
                    //первую строку в файле оставить пустой. иначе первое значение не выполняется. хз почему.
                    FileReader fileReader = new FileReader(line[1]);
                    reader = new BufferedReader(fileReader);
                } catch (FileNotFoundException e) {
                    System.out.println("Файл не найден");
                    break;
                }
                readAndExecute(reader);
                break;

            case "save":
                save();
                System.out.println("Данные сохранены в файл test на диске D");
                break;

            case "remove_greater":
                removeGreater();
                System.out.println("Все значения больше заданного удалены.");
                break;

            case "remove_lower":
                removeLower();
                System.out.println("Все значения меньше заданного удалены");
                break;

            case "print_field_ascending_best_album":
                printFieldAscendingBestAlbum();
                break;

            case "filter_greater_than_number_of_participants":
                filterGreaterThanNumberOfParticipants(Long.parseLong(line[1]));
                break;

            case "remove_any_by_genre":
                try {
                    removeAnyByGenre(line[1]);
                }
                catch (IllegalArgumentException e){
                    System.out.println("Введите жанр корректно");
                }
                break;

            case "replace_if_greater":
                removeIfGreaterByKey(Integer.parseInt(line[1]));
                break;

            default:
                System.out.println("Такая команда не подерживается");
                break;

        }
    }


    public static void printHelp() {
        System.out.println("info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)");
        System.out.println("show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении");
        System.out.println("insert key {element} : добавить новый элемент с заданным ключом");
        System.out.println("update id {element} : обновить значение элемента коллекции, id которого равен заданному");
        System.out.println("remove_key key : удалить элемент из коллекции по его ключу");
        System.out.println("clear : очистить коллекцию");
        System.out.println("save : сохранить коллекцию в файл");
        System.out.println("execute_script file_name : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.");
        System.out.println("exit : завершить программу (без сохранения в файл");
        System.out.println("remove_greater {element} : удалить из коллекции все элементы, превышающие заданный");
        System.out.println("remove_lower {element} : удалить из коллекции все элементы, меньшие, чем заданный");
        System.out.println("replace_if_greater key {element} : заменить значение по ключу, если новое значение больше старого");
        System.out.println("remove_any_by_genre genre : удалить из коллекции один элемент, значение поля genre которого эквивалентно заданному");
        System.out.println("filter_greater_than_number_of_participants numberOfParticipants : вывести элементы, значение поля numberOfParticipants которых больше заданного");
        System.out.println("print_field_ascending_best_album : вывести значения поля bestAlbum в порядке возрастания");
    }

    public static void printInfo() {
        System.out.println("Тип коллекции: " + data.getClass().getSimpleName());
        System.out.println("Дата инициализации: " + date.toString());
        System.out.println("Количество элементов: " + data.size());
    }

    public static void show() {
        for (Map.Entry<Integer, MusicBand> band : data.entrySet()) {
            System.out.println(band.getKey());
            System.out.println(band.getValue().toString());
        }
    }

    public static MusicBand insert() throws IOException {
        MusicBand musicBand = new MusicBand();
        musicBand.update();
        System.out.println("Группа успешно добавлена");
        return musicBand;
    }

    private static void readAndExecute(BufferedReader reader) throws IOException {
        String task;
        while ((task = reader.readLine()) != null) {
            System.out.println(task);
            start(task);
        }
    }
    //Можно воспользоваться проходом по колекции и записью в файл через цикл, но найденное мной решение через stream показалось мне элегантнее)
    public static void save() throws IOException {
        Files.write(Paths.get("D:/test.txt"),
                data.entrySet().stream().map(k -> k.getKey() + "\r\n" + k.getValue()).collect(Collectors.toList()),
                StandardCharsets.UTF_8);
    }

    public static void removeGreater() throws IOException {
        System.out.println("Введите данные Группы. Все группы, которые больше вашей(исходя из логики сравнения), будут удалены.");
        MusicBand musicBand = new MusicBand();
        musicBand.update();
        data.entrySet().removeIf(entry -> entry.getValue().compareTo(musicBand) < 0);
    }

    public static void removeLower() throws IOException {
        System.out.println("Введите данные Группы. Все группы, которые меньше вашей(исходя из логики сравнения), будут удалены.");
        MusicBand musicBand = new MusicBand();
        musicBand.update();
        data.entrySet().removeIf(entry -> entry.getValue().compareTo(musicBand) > 0);
    }

    public static void printFieldAscendingBestAlbum() {
        System.out.println("Альбомы в порядке возрастания");
        Comparator<Album> albumComparator = new Comparator<Album>() {
            @Override
            public int compare(Album o1, Album o2) {
                return o1.compareTo(o2);
            }
        };
        List<Album> albumList = new ArrayList<>();
        for (Map.Entry<Integer, MusicBand> band : data.entrySet()) {
            albumList.add(band.getValue().getBestAlbum());
        }
        Collections.sort(albumList, albumComparator);
        for (Album album : albumList) {
            System.out.println("Название альбома: " + album.getName() + " Продолжительность альбома: " + album.getLength());
        }
    }

    public static void filterGreaterThanNumberOfParticipants(Long numberOfParticipants) {
        System.out.println("Количество участников больше заданного");
        for (Map.Entry<Integer, MusicBand> band : data.entrySet()) {
            if (band.getValue().getNumberOfParticipants() > numberOfParticipants) {
                System.out.println(band.getValue().getNumberOfParticipants());
            }
        }
    }


    public static void removeAnyByGenre(String stringGenre) {
        MusicGenre genre = MusicGenre.valueOf(stringGenre);
        Iterator<Map.Entry<Integer, MusicBand>> iterator = data.entrySet().iterator();
        while (iterator.hasNext()) {
        Map.Entry<Integer,MusicBand> entry = iterator.next();
        if (entry.getValue().getGenre().equals(genre)){
            data.remove(entry.getKey());
            System.out.println("Элемент был удален");
            break;
        }
        }
    }
    public static void removeIfGreaterByKey(Integer key) throws IOException {
        System.out.println("Введите данные группы. Если ваша группы больше(исходя из логики сравнения), то значение по данному ключу будет заменено.");
        MusicBand musicBand = new MusicBand();
        musicBand.update();
        if(data.get(key).compareTo(musicBand)>0){
        data.get(key).changeAll(musicBand);
            System.out.println("Данные изменены");
        }
        else System.out.println("Данные не изменены");
    }
}

//execute_script D:/lol.txt
