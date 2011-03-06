package cz.krtinec.root2;

/**
 * Created by IntelliJ IDEA.
 * User: krtek
 * Date: 6.3.11
 * Time: 14:53
 * To change this template use File | Settings | File Templates.
 */
public class ContactDTO {
    public long id;
    public String name;
    public String phone;
    public String email;
    public NoteDTO note;

    public ContactDTO(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
