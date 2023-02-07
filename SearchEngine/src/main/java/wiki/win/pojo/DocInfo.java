package wiki.win.pojo;

/**
 * @program: JavaApiSearchEngine
 * @description: 文档信息
 * @author: xiedy
 * @create: 2023-02-06 10:51
 **/
public class DocInfo {

    private int docId;
    private String title;
    private String url;
    private String content;

    public DocInfo() {

    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}