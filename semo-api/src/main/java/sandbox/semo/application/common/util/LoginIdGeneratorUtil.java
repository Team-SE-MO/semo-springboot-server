package sandbox.semo.application.common.util;

public class LoginIdGeneratorUtil {

    private static int lastNumber = 1;
    
    public static String generateLoginId(String role, String taxId) {
        String prefix = "";
        if (role.equalsIgnoreCase("ADMIN")) {
            prefix = "A";
        } else if (role.equalsIgnoreCase("USER")) {
            prefix = "U";
        }

        String splitTaxId = taxId.replace("-", "");
        String lastTwoDigits = String.format("%02d", lastNumber);

        lastNumber += 1;

        return prefix + splitTaxId + lastTwoDigits;
    }
}