package com.skilltree.Service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class MinioService {

	private final MinioClient minioClient;
	private final String bucketName;

	public MinioService(MinioClient minioClient, @Value("${minio.bucket-name}") String bucketName) {
		this.minioClient = minioClient;
		this.bucketName = bucketName;
	}

	/**
	 * @param file
	 *            файл от пользователя
	 * @param folder
	 *            папка внутри бакета (например, "avatars/123")
	 * @return имя файла в хранилище (нужно, чтобы потом получить ссылку)
	 */
	public String uploadFile(MultipartFile file, String folder) throws Exception {
		// Генерируем уникальное имя файла (чтобы не было конфликтов)
		String originalFilename = file.getOriginalFilename();
		String extension = "";
		if (originalFilename != null && originalFilename.contains(".")) {
			extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		}

		String fileName = folder + "/" + UUID.randomUUID().toString() + extension;

		// Загружаем файл в MinIO
		minioClient.putObject(PutObjectArgs.builder().bucket(bucketName) // В какой бакет
				.object(fileName) // С каким именем
				.stream(file.getInputStream(), file.getSize(), -1) // Содержимое
				.contentType(file.getContentType()) // Тип файла (image/jpeg и т.д.)
				.build());

		return fileName; // Возвращаем имя, чтобы потом сформировать URL
	}

	/**
	 * Формирует публичную ссылку на файл
	 *
	 * @param fileName
	 *            имя файла (то, что вернул uploadFile)
	 * @return полная ссылка для доступа
	 */
	public String getFileUrl(String fileName) {
		return "http://localhost:9000/" + bucketName + "/" + fileName;
	}
}