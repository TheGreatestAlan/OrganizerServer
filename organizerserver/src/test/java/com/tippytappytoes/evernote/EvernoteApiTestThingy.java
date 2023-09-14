package com.tippytappytoes.evernote;

import com.evernote.edam.type.Note;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class EvernoteApiTestThingy {

  EvernoteApi everNoteApi;
  String devToken = "S=s1:U=95a34:E=18570e66ee0:C=17e193542e0:P=1cd:A=en-devtoken:V=2:H=36dbcaf1a4a08a4df3723ad4d7fa111e";
  String devToken2 = "S=s1:U=95a34:E=18573a06690:C=17e1bef3a90:P=185:A=tippytappytoes-3905:V=2:H=f0fc92117e1365966155a8ba82cd3ef4";
  String prodToken = "S=s409:U=96ba756:E=185801cd7a0:C=17e286baba0:P=185:A=tippytappytoes-3905:V=2:H=2894fe317ede5abbf5474eb94fa7c7d4";

  @BeforeEach
  public void setup() throws Exception {
    this.everNoteApi = new EvernoteApi(prodToken);

  }

  @Test
  @Disabled
  public void standard() {
    Note note = everNoteApi.findNoteByNoteTitleAndNotebookName("Organizer", "ItemLocation");
    System.out.println(note.getContent());
    List<String> notes = everNoteApi.parseNote(note.getContent());
    notes.size();
  }

}
