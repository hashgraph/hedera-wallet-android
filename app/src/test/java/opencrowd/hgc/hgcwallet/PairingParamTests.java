package opencrowd.hgc.hgcwallet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import opencrowd.hgc.hgcwallet.export_key.PairingParams;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PairingParamTests {

    @Test
    public void testDataEncryption() throws Exception {
        String key = "7E892875A52C59A3B588306B13C31FBD7E892875A52C59A3B588306B13C31FBD";
        PairingParams pairingParams = new PairingParams(Hex.decode(key),"1.2.3.4");

        String res = "{\"data\":\"4hz1nRJA3nAlKdGKdTH1wlrIJjOZPwb0CRoe1cZktA3pxJcwLl82oF7\\/SP8A+dZXTfV5YYTgMP5ft3fDrvhU8vVseWUiFA8aouRcy3d4nFQjviZKmt4h8+rVDH0wXb6hBkfJLZOh7daSKbA0j4sabfUDpUpW5Es6i5cePoGuQtI2qXUCwFVFIvfujwE\\/KkIsFHajRxOuBIdD0FbjnG9CdCHGz0pmJGU77EtjxMNqFViU9k\\/1UHR+z1d9Th9WGAx7r1fYbXEEFyVYTC9M5jqwoQ2X1j87ttccODKe5A\\/fjGmA9pY5YgtT9+S6dcSDiK50oICVw3xB5Qy9VVJM3UY5QSDilAdiRHG1dpk1NVuSqxBF5CTf5qfJ4sTKDavgln2ygfCOpKxmWMKW0PoEPOxswxgcsic7Luj2EErgoqCvhiOZn9AahnJLOz52yO9ihDrwUCEm4zjKtHPC\",\"success\":true}";

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = (JsonObject) parser.parse(res);

        String encryptedDataString = jsonObject.get("data").getAsString();
        byte[] encryptedData = Base64.getDecoder().decode(encryptedDataString);
        byte [] decryptedData = pairingParams.decrypt(encryptedData);
        byte [] messageData = Arrays.copyOf(decryptedData, decryptedData.length-48);
        String message = new String(messageData);
        assertEquals(message.contains("publicKey"), true);
    }

    @Test
    public void testGetPin() throws Exception {
        String walletIp = "10.20.30.40";

        String qr = "7E892875A52C59A3B588306B13C31FBD7E892875A52C59A3B588306B13C31FBD\n10.20.30.4";
        PairingParams pairingParams = PairingParams.Companion.fromQRCode(qr);
        assertEquals(pairingParams.getPIN(walletIp), "40");

        qr = "7E892875A52C59A3B588306B13C31FBD7E892875A52C59A3B588306B13C31FBD\n10.20.3.4";
        pairingParams = PairingParams.Companion.fromQRCode(qr);
        assertEquals(pairingParams.getPIN(walletIp), "30A40");

        qr = "7E892875A52C59A3B588306B13C31FBD7E892875A52C59A3B588306B13C31FBD\n10.2.3.4";
        pairingParams = PairingParams.Companion.fromQRCode(qr);
        assertEquals(pairingParams.getPIN(walletIp), "20A30A40");

        qr = "7E892875A52C59A3B588306B13C31FBD7E892875A52C59A3B588306B13C31FBD\n1.2.3.4";
        pairingParams = PairingParams.Companion.fromQRCode(qr);
        assertEquals(pairingParams.getPIN(walletIp), "10A20A30A40");

    }

    @Test
    public void testQRInput() throws Exception {

        String qr = "7E892875A52C59A3B588306B13C31FBD7E892875A52C59A3B588306B13C31FBD\n10.20.30.4";
        PairingParams pairingParams = PairingParams.Companion.fromQRCode(qr);
        assertNotNull(pairingParams);

        String qrInvalidIp1 = "7E892875A52C59A3B588306B13C31FBD7E892875A52C59A3B588306B13C31FBD\n10.20.30";
        pairingParams = PairingParams.Companion.fromQRCode(qrInvalidIp1);
        assertNull(pairingParams);

        String qrInvalidIp2 = "7E892875A52C59A3B588306B13C31FBD7E892875A52C59A3B588306B13C31FBD\n10.20.30.40.50";
        pairingParams = PairingParams.Companion.fromQRCode(qrInvalidIp2);
        assertNull(pairingParams);

        String qrInvalidKeyshort = "7E892875A52C59A3B588306B13C31FBD7E892875A52C59A3B588306B13C3\n10.20.30.40";
        pairingParams = PairingParams.Companion.fromQRCode(qrInvalidKeyshort);
        assertNull(pairingParams);

        String qrInvalidKeyLong = "7E892875A52C59A3B588306B13C31FBD7E892875A52C59A3B588306B13C31FBDBD\n10.20.30.40";
        pairingParams = PairingParams.Companion.fromQRCode(qrInvalidKeyLong);
        assertNull(pairingParams);

        String qrInvalidKeyEncoding = "GE892875A52C59A3B588306B13C31FBD7E892875A52C59A3B588306B13C31FBD\n10.20.30.40";
        pairingParams = PairingParams.Companion.fromQRCode(qrInvalidKeyLong);
        assertNull(pairingParams);

    }
}
