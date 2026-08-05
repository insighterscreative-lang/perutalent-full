package com.INSIGHTERS_PERU.Up.Work.Perusalen.validation;

public final class PasswordPolicy {

    public static final String REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";

    public static final String MESSAGE =
            "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial (@$!%*?&)";

    private PasswordPolicy() {
    }
}
