package com.syfri.userservice.controller.prediction;

import com.syfri.baseapi.model.ResultVO;
import com.syfri.baseapi.utils.EConstants;
import com.syfri.userservice.config.properties.CpjsProperties;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import com.syfri.userservice.model.prediction.QycpjsVO;
import com.syfri.userservice.service.prediction.QycpjsService;
import com.syfri.baseapi.controller.BaseController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("qycpjs")
public class QycpjsController extends BaseController<QycpjsVO> {

    @Autowired
    private QycpjsService qycpjsService;

    @Override
    public QycpjsService getBaseService() {
        return this.qycpjsService;
    }

    @Autowired
    private CpjsProperties cpjsProperties;

    @RequestMapping(value = "/upload")
    public QycpjsVO upload(HttpServletRequest request, QycpjsVO qycpjsVO) {
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
            //图片重命名eg. cp20181031152840.JPG
            String fileName_new = "cp"+zzsj+suffixName;
            StringBuffer new_folder = new StringBuffer();
            new_folder = new StringBuffer(qycpjsVO.getQyid()).append("/");

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
                qycpjsVO.setSrc(dbPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return qycpjsVO;
    }
    @RequestMapping(value = "/delPic")
    public boolean deleteSrc(@RequestBody List<String> list) {
        // 文件上传固定的路径
        StringBuffer relativePath = new StringBuffer(cpjsProperties.getSavePath());
        for(int i=0;i<list.size();i++){
            String path =relativePath + list.get(i);
            //删除文件
            File deletefile = new File(path);
            if(deletefile.exists()&&deletefile.isFile()) {
                deletefile.delete();
            }
        }

        return true;
    }
    //根据uuid修改产品信息 add by yushch 20181229
    @ApiOperation(value = "根据id更新基本信息", notes = "修改")
    @PostMapping("/doUpdateByVO")
    public @ResponseBody ResultVO doUpdateByVO(@RequestBody QycpjsVO vo) {
        ResultVO resultVO = ResultVO.build();
        try {
            resultVO.setResult(qycpjsService.doUpdateByVO(vo));
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
            resultVO.setCode(EConstants.CODE.FAILURE);
        }
        return resultVO;
    }
    //根据uuid删除产品信息 add by yushch 20181229
    @ApiOperation(value = "根据id更新基本信息", notes = "删除")
    @PostMapping("/doDeleteCpxx")
    public @ResponseBody ResultVO doDeleteJbxx(@RequestBody QycpjsVO vo) {
        ResultVO resultVO = ResultVO.build();
        try {
            resultVO.setResult(qycpjsService.doDeleteById(vo.getUuid()));
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
            resultVO.setCode(EConstants.CODE.FAILURE);
        }
        return resultVO;
    }
    //插入vo add by yushch 20190102
    @ApiOperation(value="根据VO保存",notes="注意事项")
    @ApiImplicitParam(name="vo",value = "业务实体")
    @PostMapping("/doInsertCpxx")
    public @ResponseBody ResultVO doInsertByVO(@RequestBody QycpjsVO vo) throws Exception{
        ResultVO resultVO = ResultVO.build();
        try {
            resultVO.setResult(qycpjsService.doInsertByVO(vo));
        } catch (Exception e) {
            logger.error("{}",e.getMessage());
            resultVO.setCode(EConstants.CODE.FAILURE);
        }

        return 	resultVO;
    }
}
