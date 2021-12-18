package blog.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private Cloudinary cloudinary;

    @Value("${blog_engine.additional.profilePhotoWidth}")
    private int PROFILE_PHOTO_WIDTH;
    @Value("${blog_engine.additional.profilePhotoHeight}")
    private int PROFILE_PHOTO_HEIGHT;


    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(String localFilePath, String remoteSubFolder) {
        return uploadImage(localFilePath, remoteSubFolder, false);
    }

    public String uploadImage(String localFilePath, String remoteSubFolder, boolean cut) {
        try {
            Map params = ObjectUtils.asMap(
                    "overwrite", true,
                    "folder", remoteSubFolder,
                    "resource_type", "image"
            );
            if (cut) {
                params.put("transformation", new Transformation().width(PROFILE_PHOTO_WIDTH).height(PROFILE_PHOTO_HEIGHT));
            }
            Map uploadResult = cloudinary.uploader().upload(new File(localFilePath), params);

            return String.valueOf(uploadResult.get("url"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
