/**
 * @author wjw
 * @date 2020/6/28 16:58
 */
package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;



@Service
public class UploadService {

    //我们只要contentType为图片的,这里定义的相当于白名单
    private static final List<String> CONTENT_TYPES = Arrays.asList("image/jpeg", "image/gif");
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class);

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private ThumbImageConfig thumbImageConfig;

    /**
     * 图片上传
     * @param file
     * @return  返回图片上传后的url
     */
    public String uploadImage(MultipartFile file){
        //文件名
        String originalFilename = file.getOriginalFilename();
        //校验文件的类型，用户可能会乱上传
        String contentType = file.getContentType();
        if (!CONTENT_TYPES.contains(contentType)){
            //文件类型不合法提示
            LOGGER.info("文件类型不合法：{}", originalFilename);
            return null;
        }

        try {
            // 校验文件的内容，有人可能将脚本/文本重命名后缀为.jpg，但是这样其实不是图片，所以我们看看能不能用ImageIO.read返回图片
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null){
                LOGGER.info("文件内容不合法：{}", originalFilename);
                return null;
            }
            // 保存到服务器
            //file.transferTo(new File("G:\\IdeaProjects\\leyou\\images\\" + originalFilename));
            StorePath path = this.storageClient.uploadImageAndCrtThumbImage(file.getInputStream(), file.getSize(), StringUtils.substringAfterLast(originalFilename, "."), null);

            // 生成url地址，返回。nginx代理后可以访问下面地址
            //return "http://image.leyou.com/" + originalFilename;
            return  "http://image.leyou.com/" + path.getFullPath();
        } catch (IOException e) {
            LOGGER.info("服务器内部错误：{}", originalFilename);
            e.printStackTrace();
        }
        return null;
    }
}
