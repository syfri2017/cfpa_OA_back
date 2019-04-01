package com.syfri.userservice.controller.prediction;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;
import com.syfri.baseapi.controller.BaseController;
import com.syfri.baseapi.model.ResultVO;
import com.syfri.baseapi.utils.EConstants;
import com.syfri.userservice.config.properties.CpjsProperties;
import com.syfri.userservice.model.prediction.QyjbxxVO;
import com.syfri.userservice.service.prediction.QyjbxxService;
import com.syfri.userservice.utils.Base64ImageUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

@RestController
@RequestMapping("qyjbxx")
public class QyjbxxController extends BaseController<QyjbxxVO> {

    @Autowired
    private QyjbxxService qyjbxxService;

    @Override
    public QyjbxxService getBaseService() {
        return this.qyjbxxService;
    }

    @Autowired
    private CpjsProperties cpjsProperties;

    /**
     * @Description: 根据企业id获取企业信息
     * @Author: rliu
     * @Date: 2018/10/9 10:35
     */
    @ApiOperation(value = "根据企业id获取企业信息", notes = "vo")
    @RequestMapping("/doFindJbxxById/{qyid}")
    public @ResponseBody
    ResultVO doFindJbxxById(@PathVariable String qyid) {
        ResultVO resultVO = ResultVO.build();
        try {
            QyjbxxVO result = qyjbxxService.doFindById(qyid);
            //将二进制转为Base64格式字符串
            String photo64 = Base64ImageUtil.byteArr2String(result.getYyzz());
            result.setYyzzBase64(photo64);
            resultVO.setResult(result);
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
            resultVO.setCode(EConstants.CODE.FAILURE);
        }
        return resultVO;
    }

    /**
     * @Description: 根据用户信息、公司名称获取企业基本信息
     * @Author: li.xue
     * @Date: 2018/10/9 11:05
     */
    @ApiOperation(value = "根据企业查询企业信息", notes = "列表")
    @ApiImplicitParam(name = "vo", value = "企业对象")
    @PostMapping("/doFindZsxxByQyjbxx")
    public @ResponseBody
    ResultVO doFindZsxxByQyjbxx(@RequestBody QyjbxxVO qyjbxxVO) {
        ResultVO resultVO = ResultVO.build();
        try {
            PageHelper.startPage(qyjbxxVO.getPageNum(), qyjbxxVO.getPageSize());
            List<QyjbxxVO> list = qyjbxxService.doFindZsxxByQyjbxx(qyjbxxVO);
            PageInfo<QyjbxxVO> pageInfo = new PageInfo<>(list);
            resultVO.setResult(pageInfo);
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
            resultVO.setCode(EConstants.CODE.FAILURE);
        }
        return resultVO;
    }

    /**
     * @Description: 根据企业id更新基本信息
     * @Author: rliu
     * @Date: 2018/10/9 10:35
     */
    @ApiOperation(value = "", notes = "修改")
    @ApiImplicitParam(name = "vo", value = "企业对象")
//	@RequiresPermissions("prediction/exhprediction_approve:approve")
    @PostMapping("/updateByVO")
    public @ResponseBody
    ResultVO updateByVO(@RequestBody QyjbxxVO qyjbxxVO) {
        ResultVO resultVO = ResultVO.build();
        try {
            resultVO.setResult(qyjbxxService.doUpdateByVO(qyjbxxVO));
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
            resultVO.setCode(EConstants.CODE.FAILURE);
        }
        return resultVO;
    }

    //add by rliu 20181227
    @ApiOperation(value = "展会报名管理导出", notes = "导出")
    @RequestMapping(value = "/doExportJbxx/{param}", method = RequestMethod.GET)
    public void doExport(HttpServletRequest request, HttpServletResponse response, @PathVariable String[] param) {
        //解析param zwh&zwzt&qymc&zwlb&cklx
        QyjbxxVO vo = new QyjbxxVO();
        vo.setGsmc(param[0]);
        vo.setSjzt(param[1]);
        vo.setShzt(param[2]);

        //excel标题
        String[] title = {"公司名称", "联系人", "联系人手机", "数据状态", "审核状态"};
        //columns标题
        String[] columns = {"zwgsmc", "lxr", "lxrsj", "sjztmc", "shztmc"};
        //excel文件名
        String fileName = "展会报名管理导出" + System.currentTimeMillis() + ".xls";
        //sheet名
        String sheetName = "展会报名管理";

        //获取数据
        List<QyjbxxVO> dataList = qyjbxxService.doSearchListByVO(vo);
        for (int i = 0; i < dataList.size(); i++) {
            if ("ENG".equals(dataList.get(i).getUsertype())) {
                dataList.get(i).setZwgsmc(dataList.get(i).getYwgsmc());
            }
        }
        this.doExportExcel(request, response, fileName, sheetName, title, columns, dataList);
    }

    //add by yushch 20181010
    @ApiOperation(value = "根据VO保存", notes = "保存")
    @PostMapping("/doInsertByVo")
    public @ResponseBody
    ResultVO save(@RequestBody QyjbxxVO vo) throws Exception {
        ResultVO resultVO = ResultVO.build();
        try {
            resultVO.setResult(qyjbxxService.doInsertJbxxByVO(vo));
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
            resultVO.setCode(EConstants.CODE.FAILURE);
        }

        return resultVO;
    }

    @ApiOperation(value = "根据id更新基本信息", notes = "修改")
    @PostMapping("/doUpdateByVO")
    public @ResponseBody
    ResultVO doUpdateByVO(@RequestBody QyjbxxVO vo) {
        ResultVO resultVO = ResultVO.build();
        try {
            resultVO.setResult(qyjbxxService.doUpdateByVO(vo));
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
            resultVO.setCode(EConstants.CODE.FAILURE);
        }
        return resultVO;
    }

    @ApiOperation(value = "根据id更新基本信息", notes = "删除")
    @PostMapping("/doDeleteJbxx")
    public @ResponseBody
    ResultVO doDeleteJbxx(@RequestBody List<QyjbxxVO> voList) {
        ResultVO resultVO = ResultVO.build();
        try {
            resultVO.setResult(qyjbxxService.doDeleteJbxx(voList));
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
            resultVO.setCode(EConstants.CODE.FAILURE);
        }
        return resultVO;
    }

    //上传图片并加水印 by yushch
    @RequestMapping(value = "/upload")
    @ResponseBody
    public QyjbxxVO uploadAttachment(HttpServletRequest request, QyjbxxVO vo)
            throws UnsupportedEncodingException {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Iterator<String> iterator = multipartRequest.getFileNames();
        while (iterator.hasNext()) {
            MultipartFile multipartFile = multipartRequest.getFile(iterator.next());
            if ("".equals(multipartFile.getOriginalFilename())) throw new RuntimeException("文件为空");
            String fileName = multipartFile.getOriginalFilename();
            String fileTyle = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            if (fileTyle.toLowerCase().equals(".pdf")) {
                vo.setYyzzgs(".pdf");
                vo = qyjbxxService.uploadPdfs(multipartFile, vo, fileName);
            } else {
                vo = qyjbxxService.uploadPics(multipartFile, vo, fileName);
            }
        }
        return vo;

    }

    //add by yushch 20181014
    @ApiOperation(value = "根据userid获取企业信息", notes = "vo")
    @PostMapping("/doFindByVo")
    public @ResponseBody ResultVO doFindByVo(@RequestBody QyjbxxVO vo) {
        ResultVO resultVO = ResultVO.build();
        try {
            QyjbxxVO result = qyjbxxService.doFindByVO(vo);
            /*
            if (result != null) {
                //将二进制转为Base64格式字符串
                String photo64 = Base64ImageUtil.byteArr2String(result.getYyzz());
                result.setYyzzBase64(photo64);
            }*/
            resultVO.setResult(result);
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
            resultVO.setCode(EConstants.CODE.FAILURE);
        }
        return resultVO;
    }

    //查询当前邮箱是否被注册（用户表、基本信息表）
    //add by yushch 20181018
    @ApiOperation(value = "根据邮箱查询用户数量", notes = "查询")
    @ApiImplicitParam(name = "mail", value = "邮箱")
    @GetMapping("/getMailNum/{mail}/static")
    public @ResponseBody
    ResultVO getMailNum(@PathVariable String mail) {
        ResultVO resultVO = ResultVO.build();
        try {
            resultVO.setResult(qyjbxxService.doSearchCountByMail(mail));
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
            resultVO.setCode(EConstants.CODE.FAILURE);
        }
        return resultVO;
    }

    //新建基本信息时营业执照存在根目录下，插入基本信息时移动图片到qyid文件夹下
    //add by yushch 20181102
    @RequestMapping(value = "/movePic")
    public QyjbxxVO movePic(@RequestBody QyjbxxVO vo) {
        // 文件上传固定的路径
        StringBuffer relativePath = new StringBuffer(cpjsProperties.getSavePath());
        StringBuffer new_folder = new StringBuffer();
        new_folder = new StringBuffer(vo.getQyid()).append("/");
        String folderName = relativePath.toString() + new_folder;
        //创建文件夹
        File dir = new File(folderName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(relativePath + vo.getSrc());
        String destinationFloderUrl = new StringBuffer(folderName).append(vo.getSrc()).toString();
        //检查源文件是否合法
        if (file.isFile() && file.exists()) {
            String destinationFile = destinationFloderUrl;
            if (!file.renameTo(new File(destinationFile))) {
                this.logger.error("移动文件失败！");
                return vo;
            }
        } else {
            this.logger.error("要备份的文件路径不正确，移动失败！");
            return vo;
        }
        if (vo.getYyzzgs().equals(".pdf")) {
            String name = vo.getSrc().substring(0, vo.getSrc().lastIndexOf("."));
            String pdfSrc = name + ".pdf";
            File pdfFile = new File(relativePath + pdfSrc);
            String destinationFloderUrl2 = new StringBuffer(folderName).append(pdfSrc).toString();
            //检查源文件是否合法
            if (pdfFile.isFile() && pdfFile.exists()) {
                String destinationFile2 = destinationFloderUrl2;
                if (!pdfFile.renameTo(new File(destinationFile2))) {
                    this.logger.error("移动文件失败！");
                    return vo;
                }
            } else {
                this.logger.error("要备份的文件路径不正确，移动失败！");
                return vo;
            }
        }
        String dbPath = new_folder.append(vo.getSrc()).toString();
        vo.setSrc(dbPath);
        qyjbxxService.doUpdateByVO(vo);
        return vo;
    }

    /**
     * @Description: 统计分析查询是否信息确认
     * @Author: rliu
     * @Date: 2019/1/4 10:35
     */
    @ApiOperation(value = "统计分析查询是否信息确认", notes = "vo")
    @RequestMapping("/ifConfirmedTjfx")
    public @ResponseBody
    ResultVO ifConfirmedTjfx(@RequestBody QyjbxxVO vo) {
        ResultVO resultVO = ResultVO.build();
        try {
            List<QyjbxxVO> result = qyjbxxService.ifConfirmedTjfx(vo);
            resultVO.setResult(result);
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
            resultVO.setCode(EConstants.CODE.FAILURE);
        }
        return resultVO;
    }

    /**
     * @Description: 统计分析查询是否信息确认_详情
     * @Author: rliu
     * @Date: 2019/1/7 10:35
     */
    @ApiOperation(value = "统计分析查询是否信息确认_详情", notes = "列表")
    @RequestMapping("/ifConfirmedTjfxDetail")
    public @ResponseBody
    ResultVO ifConfirmedTjfxDetail(@RequestBody QyjbxxVO qyjbxxVO) {
        ResultVO resultVO = ResultVO.build();
        try {
            PageHelper.startPage(qyjbxxVO.getPageNum(), qyjbxxVO.getPageSize());
            List<QyjbxxVO> list = qyjbxxService.ifConfirmedTjfxDetail(qyjbxxVO);
            PageInfo<QyjbxxVO> pageInfo = new PageInfo<>(list);
            resultVO.setResult(pageInfo);
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
            resultVO.setCode(EConstants.CODE.FAILURE);
        }
        return resultVO;
    }

    //add by rliu 20190107
    @ApiOperation(value = "是否信息确认详情导出", notes = "导出")
    @RequestMapping(value = "/doExportConfirmDetail/{param}", method = RequestMethod.GET)
    public void doExportConfirmDetail(HttpServletRequest request, HttpServletResponse response, @PathVariable String[] param) {
        //解析param
        QyjbxxVO vo = new QyjbxxVO();
        vo.setQrzt(param[0]);
        vo.setQrsj_start(param[1]);
        vo.setQrsj_end(param[2]);

        String qrzt = vo.getQrzt().equals("1") ? "信息已确认导出_" : "信息未确认导出_";
        String qrsjStart = "";
        String qrsjEnd = "";
        if (!vo.getQrsj_start().equals("") && vo.getQrsj_start() != null) {
            qrsjStart = vo.getQrsj_start().substring(0, vo.getQrsj_start().lastIndexOf("-") + 3).toString() + "至";
        }
        if (!vo.getQrsj_end().equals("") && vo.getQrsj_end() != null) {
            qrsjEnd = vo.getQrsj_end().substring(0, vo.getQrsj_end().lastIndexOf("-") + 3).toString();
        } else {
            qrsjEnd = "至今";
        }

        //excel文件名
        String fileName = qrzt + qrsjStart + qrsjEnd + ".xls";
        //sheet名
        String sheetName = (vo.getQrzt().equals("1") ? "已确认_" : "未确认_") + qrsjStart + qrsjEnd;
        //excel标题
        String[] title = {"公司名称", "联系人", "联系人手机", "上次确认时间"};
        //columns标题
        String[] columns = {"zwgsmc", "lxr", "lxrsj", "qrsj"};
        //获取数据
        List<QyjbxxVO> dataList = qyjbxxService.ifConfirmedTjfxDetail(vo);
        for (int i = 0; i < dataList.size(); i++) {
            if ("ENG".equals(dataList.get(i).getUsertype())) {
                dataList.get(i).setZwgsmc(dataList.get(i).getYwgsmc());
            }
        }
        this.doExportExcel(request, response, fileName, sheetName, title, columns, dataList);
    }

    /**********************************************************************************************
     //上传pdf并加水印 by huangrui
     @RequestMapping(value = "/uploadPdf")
     @ResponseBody public void uploadAttachmentPdf(HttpServletRequest request, QyjbxxVO vo)
     throws UnsupportedEncodingException {
     // 水印文字
     String waterText = "仅用于第十八届国际消防展信息比对";
     // 水印透明度
     float alpha = 0.5f;
     // 水印颜色
     BaseColor color = new BaseColor(255, 0, 0);

     Map<String, Object> result = new HashMap<String, Object>();
     try {
     MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
     Iterator<String> iterator = multipartRequest.getFileNames();
     while (iterator.hasNext()) {
     MultipartFile file = multipartRequest.getFile(iterator.next());
     // 保存文件
     String path = this.savenew(file);
     // 待加水印的文件
     PdfReader reader = new PdfReader(
     path);
     ByteArrayOutputStream bos = new ByteArrayOutputStream();
     // 加完水印的文件
     PdfStamper stamper = new PdfStamper(reader, bos);
     int total = reader.getNumberOfPages() + 1;
     PdfContentByte content;
     // 设置字体
     BaseFont base = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",
     BaseFont.EMBEDDED);
     int j = waterText.length(); // 文字长度
     char c = 0;
     int high = 0;// 高度
     // 循环对每页插入水印
     for (int i = 1; i < total; i++) {
     // 水印的起始
     high = 500;
     content = stamper.getUnderContent(i);
     // 开始
     content.beginText();
     // 设置颜色
     content.setColorFill(color);
     // 设置字体及字号
     content.setFontAndSize(base, 18);
     // 设置起始位置
     content.setTextMatrix(400, 780);
     PdfGState gs = new PdfGState();
     gs.setFillOpacity(alpha);// 设置透明度为0.2
     content.setGState(gs);
     // 开始写入水印
     for (int k = 0; k < j; k++) {
     content.setTextRise(14);
     c = waterText.charAt(k);
     // 将char转成字符串
     content.showText(c + "");
     high -= 5;
     }
     content.endText();
     }
     stamper.close();
     byte[] pdfbuffer = bos.toByteArray();
     vo.setYyzz(pdfbuffer);
     qyjbxxService.doUpdateByVO(vo);
     //删除文件
     File deletefile = new File(path);
     if(deletefile.exists()&&deletefile.isFile()) {
     deletefile.delete();
     }
     }
     } catch (IOException e) {
     e.printStackTrace();
     } catch (DocumentException e) {
     e.printStackTrace();
     }
     }
     //保存pdf by huangrui
     public static String savenew(MultipartFile file) {
     try {
     // 获取文件名
     String fileName = file.getOriginalFilename();
     if ("".equals(fileName)) {
     System.out.print("文件为空！");
     return "";
     }
     // 获取文件的后缀名
     String suffixName = fileName.substring(fileName.lastIndexOf("."));
     if (suffixName.equalsIgnoreCase("pdf")) {
     System.out.print("格式不正确！");
     return "";
     }
     // 设置文件存储路径
     String basePath = "E://test//upload//pdf";
     Random rand = new Random();
     String childPath = String.valueOf(rand.nextInt(100) + 1);
     String id = UUID.randomUUID().toString();
     id = id.replace("-", "");
     String path = basePath + childPath + id + suffixName;
     File dest = new File(path);
     // 检测是否存在目录
     if (!dest.getParentFile().exists()) {
     dest.getParentFile().mkdirs();// 新建文件夹
     }
     // 文件写入
     file.transferTo(dest);
     System.out.print("上传成功！");
     return path;
     } catch (IllegalStateException e) {
     e.printStackTrace();
     } catch (IOException e) {
     e.printStackTrace();
     }
     System.out.print("上传失败！");
     return "";
     }

     @PostMapping("/byteToPdf") public List<byte[]> byteToPdf(byte[] buffer){
     List<byte[]> list = null;
     String basePath = "E://test//upload//pdf";
     Random rand = new Random();
     String childPath = String.valueOf(rand.nextInt(100) + 1);
     String id = UUID.randomUUID().toString();
     id = id.replace("-", "");
     String suffixName = ".pdf";

     //正式用法
     //		//从数据库获取文件到本地
     //		File PDFfFile = getFile(buffer,basePath,id + suffixName);
     //		//将本地的pdf转成image，并返回List<byte[]>
     //		list = pdf2image(basePath,id,0,1,"jpg");
     //		//删除文件
     //		if(PDFfFile.exists()&&PDFfFile.isFile()) {
     //			PDFfFile.delete();
     //		}
     //		return list;

     //测试
     //给buffer赋值
     QyjbxxVO qyjbxxVO = new QyjbxxVO();
     qyjbxxVO.setUserid("22BCC665CB184553844ADF7C47E1927B");
     ResultVO resultVO = doFindByUserid(qyjbxxVO);
     qyjbxxVO = (QyjbxxVO) resultVO.getResult();
     buffer = qyjbxxVO.getYyzz();
     //从数据库获取文件到本地
     File PDFfFile = getFile(buffer,basePath,id + suffixName);
     //将本地的pdf转成image，并返回List<byte[]>
     list = pdf2image(basePath,"8131a583909cc348838073a79db9aacc26",0,1,"jpg");
     //删除文件
     if(PDFfFile.exists()&&PDFfFile.isFile()) {
     PDFfFile.delete();
     }
     return list;
     }


     /**
      *获取byte格式的文件并存储到本地
      * @param bfile 文件
     * @param filePath pdf文件保存位置
     * @param fileName pdf文件名
     *
    public static File getFile(byte[] bfile, String filePath,String fileName) {
    BufferedOutputStream bos = null;
    FileOutputStream fos = null;
    File file = null;
    try {
    File dir = new File(filePath);
    if(!dir.exists()&&dir.isDirectory()){//判断文件目录是否存在
    dir.mkdirs();
    }
    file = new File(filePath+"\\"+fileName);
    fos = new FileOutputStream(file);
    bos = new BufferedOutputStream(fos);
    bos.write(bfile);
    } catch (Exception e) {
    e.printStackTrace();
    } finally {
    if (bos != null) {
    try {
    bos.close();
    } catch (IOException e1) {
    e1.printStackTrace();
    }
    }
    if (fos != null) {
    try {
    fos.close();
    } catch (IOException e1) {
    e1.printStackTrace();
    }
    }
    }
    return file;
    }

    /**
     *将本地的pdf转成image，能够自由确定起始页和终止页
     * @param fileAddress 文件地址
     * @param filename pdf文件名
     * @param indexOfStart 开始页  开始转换的页码，从0开始
     * @param indexOfEnd 结束页  停止转换的页码，-1为全部
     * @param type 图片类型
     *
    public static List<byte[]> pdf2image(String fileAddress,String filename,int indexOfStart,int indexOfEnd,String type) {
    // 将pdf装图片 并且自定义图片得格式大小
    File file = new File(fileAddress+"\\"+filename+".pdf");
    // 创建返回值列表
    List<byte[]> list = new ArrayList<>();
    try {
    PDDocument doc = PDDocument.load(file);
    PDFRenderer renderer = new PDFRenderer(doc);
    int pageCount = doc.getNumberOfPages();
    for (int i = indexOfStart; i < indexOfEnd; i++) {
    BufferedImage image = renderer.renderImageWithDPI(i, 144); // Windows native DPI
    // BufferedImage srcImage = resize(image, 240, 240);//产生缩略图
    //A.写到文件夹
    //				ImageIO.write(image, type, new File(fileAddress+"\\"+filename+"_"+(i+1)+"."+type));
    //B.转byte[]并返回
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ImageIO.write(image, type, bos);
    bos.flush();
    bos.close();
    byte[] buffer = bos.toByteArray();
    list.add(buffer);
    }
    } catch (IOException e) {
    e.printStackTrace();
    }
    return list;
    }
     **********************************************************************************************/
}
