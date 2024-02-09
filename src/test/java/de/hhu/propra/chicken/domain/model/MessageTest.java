package de.hhu.propra.chicken.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageTest {

    @Test
    @DisplayName("Wenn Descsription hinzugefuegt wurde, ist sie vorhanden")
    void test1(){
        //Arrange
        String text = "Was fuer ein toller test";
        Message message = new Message(text);
        //Act
        String errorDescription = message.getErrorDescription();
        //Assert
        assertThat(errorDescription).isEqualTo(text);

    }
}
