package de.hhu.propra.chicken.web.forms;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public record RegistrationForm(@NotNull @Positive Long lsfID) {
}
