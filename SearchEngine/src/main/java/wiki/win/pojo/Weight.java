package wiki.win.pojo;

/**
 * @program: JavaApiSearchEngine
 * @description: 相关性权重
 * @author: xiedy
 * @create: 2023-02-06 10:54
 **/
public class Weight {

    private int docId;

    private int weight;

    public Weight() {
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
