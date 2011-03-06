package cz.krtinec.root2;

/**
 * Created by IntelliJ IDEA.
 * User: krtek
 * Date: 6.3.11
 * Time: 16:13
 * To change this template use File | Settings | File Templates.
 */
public class NoteDTO {
    public long id;
    public String note;

    public NoteDTO(long id, String note) {
        this.id = id;
        this.note = note;
    }
}
