package com.syfri.userservice.controller.prediction;

import com.syfri.baseapi.model.ResultVO;
import com.syfri.baseapi.utils.EConstants;
import com.syfri.userservice.config.properties.CpjsProperties;
import com.syfri.userservice.utils.Base64ImageUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import com.syfri.userservice.model.prediction.QyjsVO;
import com.syfri.userservice.service.prediction.QyjsService;
import com.syfri.baseapi.controller.BaseController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

@RestController
@RequestMapping("qyjs")
public class QyjsController extends BaseController<QyjsVO> {

    @Autowired
    private QyjsService qyjsService;

    @Override
    public QyjsService getBaseService() {
        return this.qyjsService;
    }

    @Autowired
    private CpjsProperties cpjsProperties;
    /**
     * @Description: 根据企业id获取产品信息
     * @Author: rliu
     * @Date: 2018/10/9 10:35
     */
    @ApiOperation(value = "根据企业id获取产品信息", notes = "vo")
    @RequestMapping("/doFindQyjsById/{qyid}")
    public @ResponseBody ResultVO doFindQyjsById(@PathVariable String qyid) {
        ResultVO resultVO = ResultVO.build();
        try {
            QyjsVO result = qyjsService.doFindById(qyid);
            if (result != null) {
                //将二进制转为Base64格式字符串
                String photo64 = Base64ImageUtil.byteArr2String(result.getLogo());
                result.setLogoBase64(photo64);
            }
            resultVO.setResult(result);
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
            resultVO.setCode(EConstants.CODE.FAILURE);
        }
        return resultVO;
    }

    //add by yushch 20181011
    @ApiOperation(value="根据VO保存",notes="保存")
    @PostMapping("/doInsertByVo")
    public @ResponseBody ResultVO save(@RequestBody QyjsVO vo) throws Exception{
        ResultVO resultVO = ResultVO.build();
        try {
            resultVO.setResult(qyjsService.doInsertQyAndCpByVO(vo));
        } catch (Exception e) {
            logger.error("{}",e.getMessage());
            resultVO.setCode(EConstants.CODE.FAILURE);
        }

        return 	resultVO;
    }

    @ApiOperation(value="根据VO更新",notes="更新")
    @ApiImplicitParam(name="vo",value = "企业产品信息")
    @PostMapping("/doUpdateQyCpByVO")
    public @ResponseBody ResultVO doUpdateQyCpByVO(@RequestBody QyjsVO vo) throws Exception{
        ResultVO resultVO = ResultVO.build();
        try {
            resultVO.setResult(qyjsService.doUpdateQyCpByVO(vo));
        } catch (Exception e) {
            logger.error("{}",e.getMessage());
            resultVO.setCode(EConstants.CODE.FAILURE);
        }
        return 	resultVO;
    }
    @RequestMapping(value = "/upload")
    @ResponseBody
    public QyjsVO uploadAttachment(HttpServletRequest request, QyjsVO vo) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Iterator<String> iterator = multipartRequest.getFileNames();
        while (iterator.hasNext()) {
            MultipartFile multipartFile = multipartRequest.getFile(iterator.next());
            // 获取文件名
            String fileName = multipartFile.getOriginalFilename();
            if ("".equals(multipartFile.getOriginalFilename())) throw new RuntimeException("文件为空");
            InputStream is = null;
            try {
                is = multipartFile.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 文件上传固定的路径
            StringBuffer relativePath = new StringBuffer(cpjsProperties.getSavePath());
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String zzsj = sdf.format(date);
            String suffixName = fileName.substring(fileName.lastIndexOf("."));
            //图片重命名eg. logo20181031152840.JPG
            String fileName_new = "logo" + zzsj + suffixName;
            StringBuffer new_folder = new StringBuffer();
            new_folder = new StringBuffer(vo.getQyid()).append("/");

            String folderName = relativePath.append(new_folder).toString();
            //创建文件夹
            File dir = new File(folderName);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            //数据库要存的数据
            String dbPath = new_folder.append(fileName_new).toString();
            //文件全路径
            StringBuffer allPath = new StringBuffer(folderName).append(fileName_new);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(allPath.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            byte[] b = new byte[1024];
            try {
                while ((is.read(b)) != -1) {
                    fos.write(b);
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                fos.close();
                is.close();
                //插入数据
                vo.setSrc(dbPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return vo;
    }
}
