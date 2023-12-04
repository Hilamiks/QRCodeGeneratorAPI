package qrcodeapi;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
class Controller {
    @GetMapping("/api/health")
    public HttpStatusCode checkHealth() {
        return HttpStatus.OK;
    }

    @GetMapping("/api/qrcode")
    public ResponseEntity<Object> getQR(@RequestParam(required = false, defaultValue = "250") int size,
                                        @RequestParam(required = false, defaultValue = "png") String type,
                                        @RequestParam(required = false, defaultValue = "") String contents,
										@RequestParam(required = false, defaultValue = "L") String correction) {
        if(contents.isBlank()) {
            return new ResponseEntity<>(new HashMap<String, String>() {{
                put("error", "Contents cannot be null or blank");
            }}, HttpStatus.BAD_REQUEST);
        } else if (size < 150 || size > 350) {
            return new ResponseEntity<>(new HashMap<String, String>() {{
                put("error", "Image size must be between 150 and 350 pixels");
            }}, HttpStatus.BAD_REQUEST);
        } else if(!correction.equals("L")
		&& !correction.equals("M")
		&& !correction.equals("Q")
		&& !correction.equals("H")) {
			return new ResponseEntity<>(new HashMap<String, String>() {{
				put("error", "Permitted error correction levels are L, M, Q, H");
			}}, HttpStatus.BAD_REQUEST);
		} else {
			switch (type) {
				case "png" -> {
					BufferedImage bufferedImage = new QR().createQR(size, contents, correction);
					return ResponseEntity
							.ok()
							.contentType(MediaType.IMAGE_PNG)
							.body(bufferedImage);
				}
				case "jpeg" -> {
					BufferedImage bufferedImage = new QR().createQR(size, contents, correction);
					return ResponseEntity
							.ok()
							.contentType(MediaType.IMAGE_JPEG)
							.body(bufferedImage);
				}
				case "gif" -> {
					BufferedImage bufferedImage = new QR().createQR(size, contents, correction);
					return ResponseEntity
							.ok()
							.contentType(MediaType.IMAGE_GIF)
							.body(bufferedImage);
				}
				default -> {
					return new ResponseEntity<>(new HashMap<String, String>() {{
						put("error", "Only png, jpeg and gif image types are supported");
					}}, HttpStatus.BAD_REQUEST);
				}
			}
        }
    }

    @Bean
    public HttpMessageConverter<BufferedImage> bufferedImageHttpMessageConverter() {
        return new BufferedImageHttpMessageConverter();
    }
}


class QR {
    BufferedImage createQR(int size, String data, String correction) {
		BufferedImage bufferedImage = null;
		QRCodeWriter writer = new QRCodeWriter();

		Map<EncodeHintType, ?> hints = switch (correction) {
			case "H" -> Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
			case "L" -> Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
			case "M" -> Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
			case "Q" -> Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
			default -> null;
		};
		try {
			BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints);
			bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
		} catch (WriterException e) {
			// handle the WriterException
		}
        return bufferedImage;
    }
}