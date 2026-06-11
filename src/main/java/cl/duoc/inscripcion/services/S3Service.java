package cl.duoc.inscripcion.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.List;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucket;

    public S3Service(
            @Value("${aws.region}") String region,
            @Value("${aws.s3.bucket}") String bucket) {
        this.bucket = bucket;
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * Sube un archivo al bucket S3.
     *
     * @param inscripcionId numero de la inscripcion (nombre de la carpeta)
     * @param contenido     bytes del archivo
     * @param contentType   MIME type (ej: "text/plain")
     * @return clave (key) del objeto guardado en S3
     */
    public String subirArchivo(Long inscripcionId, byte[] contenido, String contentType) {
        String key = inscripcionId + "/resumen-" + inscripcionId + ".txt";

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(contenido)
        );

        return key;
    }

    /**
     * Descarga un archivo desde S3 como InputStream.
     *
     * @param inscripcionId numero de la inscripcion
     * @return InputStream con el contenido del archivo
     * @throws NoSuchKeyException si el archivo no existe
     */
    public InputStream descargarArchivo(Long inscripcionId) {
        String key = inscripcionId + "/resumen-" + inscripcionId + ".txt";

        return s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
    }

    /**
     * Reemplaza el archivo existente en S3 con contenido nuevo.
     * (putObject sobreescribe si la key ya existe, pero lo dejamos
     * explicito con un metodo separado para claridad de la API)
     *
     * @param inscripcionId numero de la inscripcion
     * @param contenido     nuevo contenido
     * @param contentType   MIME type
     * @return clave actualizada
     */
    public String actualizarArchivo(Long inscripcionId, byte[] contenido, String contentType) {
        return subirArchivo(inscripcionId, contenido, contentType);
    }

    /**
     * Elimina el archivo de resumen de una inscripcion desde S3.
     *
     * @param inscripcionId numero de la inscripcion
     * @throws NoSuchKeyException si el archivo no existe
     */
    public void eliminarArchivo(Long inscripcionId) {
        String key = inscripcionId + "/resumen-" + inscripcionId + ".txt";

        // Verificar que existe antes de borrar
        s3Client.headObject(
                HeadObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );

        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
    }

    public String getBucket() {
        return bucket;
    }

    public List<String> listarArchivos() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        return response.contents().stream()
                .map(S3Object::key)
                .collect(java.util.stream.Collectors.toList());
    }
}