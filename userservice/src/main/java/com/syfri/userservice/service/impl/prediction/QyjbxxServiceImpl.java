package com.syfri.userservice.service.impl.prediction;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;
import com.syfri.userservice.config.properties.CpjsProperties;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.syfri.baseapi.service.impl.BaseServiceImpl;
import com.syfri.userservice.dao.prediction.QyjbxxDAO;
import com.syfri.userservice.dao.prediction.QyzwyxDAO;
import com.syfri.userservice.dao.prediction.QycpjsDAO;
import com.syfri.userservice.dao.prediction.QyjsDAO;
import com.syfri.userservice.dao.prediction.QykpxxDAO;
import com.syfri.userservice.dao.prediction.QywjdcDAO;
import com.syfri.userservice.model.prediction.QyjbxxVO;
import com.syfri.userservice.model.prediction.QyzwyxVO;
import com.syfri.userservice.model.prediction.QycpjsVO;
import com.syfri.userservice.model.prediction.QyjsVO;
import com.syfri.userservice.model.prediction.QykpxxVO;
import com.syfri.userservice.model.prediction.QywjdcVO;
import com.syfri.userservice.service.prediction.QyjbxxService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Transactional(rollbackFor = {Exception.class, RuntimeException.class})
@Service("qyjbxxService")
public class QyjbxxServiceImpl extends BaseServiceImpl<QyjbxxVO> implements QyjbxxService {

	@Autowired
	private QyjbxxDAO qyjbxxDAO;
	@Autowired
	private QykpxxDAO qykpxxDAO;
	@Autowired
	private QycpjsDAO qycpjsDAO;
	@Autowired
	private QyjsDAO qyjsDAO;
	@Autowired
	private QywjdcDAO qywjdcDAO;
	@Autowired
	private QyzwyxDAO qyzwyxDAO;

	@Autowired
	private CpjsProperties cpjsProperties;

	@Override
	public QyjbxxDAO getBaseDAO() {
		return qyjbxxDAO;
	}

	/*--通过用户、企业查询展商信息 by li.xue 2018/10/0 11:13.--*/
	@Override
	public List<QyjbxxVO> doFindZsxxByQyjbxx(QyjbxxVO qyjbxxVO) {
		return qyjbxxDAO.doFindZsxxByQyjbxx(qyjbxxVO);
	}

	public int doDeleteJbxx(List<QyjbxxVO> voList) {
		int sum = 0;
		for (QyjbxxVO vo : voList) {
			//开票信息删除
			QykpxxVO kpxxVo = new QykpxxVO();
			kpxxVo.setQyid(vo.getQyid());
			kpxxVo.setXgrid(vo.getXgrid());
			kpxxVo.setXgrmc(vo.getXgrmc());
			qykpxxDAO.doDeleteByVO(kpxxVo);
			//问卷调查删除
			QywjdcVO wjdcVO = new QywjdcVO();
			wjdcVO.setQyid(vo.getQyid());
			wjdcVO.setXgrid(vo.getXgrid());
			wjdcVO.setXgrmc(vo.getXgrmc());
			qywjdcDAO.doDeleteByVO(wjdcVO);
			//企业介绍删除
			QyjsVO jsVO = new QyjsVO();
			jsVO.setQyid(vo.getQyid());
			jsVO.setXgrid(vo.getXgrid());
			jsVO.setXgrmc(vo.getXgrmc());
			qyjsDAO.doDeleteByVO(jsVO);
			//产品介绍删除
			QycpjsVO cpjsVO = new QycpjsVO();
			cpjsVO.setQyid(vo.getQyid());
			cpjsVO.setXgrid(vo.getXgrid());
			cpjsVO.setXgrmc(vo.getXgrmc());
			qycpjsDAO.doDeleteByVO(cpjsVO);
			//展位意向删除
			QyzwyxVO zwyxVO = new QyzwyxVO();
			zwyxVO.setQyid(vo.getQyid());
			zwyxVO.setXgrid(vo.getXgrid());
			zwyxVO.setXgrmc(vo.getXgrmc());
			qyzwyxDAO.doDeleteByVO(zwyxVO);
			//基本信息删除
			sum = sum + qyjbxxDAO.doUpdateByVO(vo);
		}
		return sum;
	}

	@Override
	public QyjbxxVO doInsertJbxxByVO(QyjbxxVO qyjbxxVO) {
		qyjbxxDAO.doInsertByVO(qyjbxxVO);
		return qyjbxxVO;
	}

	//add by yushch 20181018
	@Override
	public int doSearchCountByMail(String mail) {
		return qyjbxxDAO.doSearchCountByMail(mail);
	}

	@Override
	public QyjbxxVO uploadPics(MultipartFile multipartFile, QyjbxxVO vo,String fileName) {
		try {
			InputStream fis = null;
			fis = multipartFile.getInputStream();
			//加水印
			Image image = ImageIO.read(fis);
			BufferedImage bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = bi.createGraphics();
			g2.drawImage(image, 0, 0, image.getWidth(null), image.getHeight(null), null);
			//颜色
			Color color = new Color(105, 105, 105);
			g2.setColor(color);
			//字体大小为图片宽的1/11
			g2.setFont(new Font("宋体", Font.BOLD, image.getWidth(null) / 11));
			//透明度
			float alpha = 0.5f;
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
					alpha));
			//int degree = -60;
			//g2.rotate(Math.toRadians(degree));//设置水印旋转
			String text = "仅供十八届消防展审核";
			g2.drawString(text, 0, image.getHeight(null) / 2);
			g2.dispose();
			// 文件上传固定的路径
			StringBuffer relativePath = new StringBuffer(cpjsProperties.getSavePath());
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String zzsj = sdf.format(date);
			String suffixName = fileName.substring(fileName.lastIndexOf("."));
			//图片重命名eg. yyzz20181031152840.JPG
			String fileName_new = "yyzz" + zzsj + suffixName;
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
			FileOutputStream out = new FileOutputStream(allPath.toString());
			ImageIO.write(bi, "jpg", out);
			out.close();
			fis.close();
			vo.setSrc(dbPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return vo;
	}
	@Override
	public QyjbxxVO uploadPdfs(MultipartFile multipartFile, QyjbxxVO vo,String fileName) {
		try {
			InputStream fis = null;
			fis = multipartFile.getInputStream();
			PdfReader reader = new PdfReader(fis);

			//ByteArrayOutputStream bos = new ByteArrayOutputStream();
			// 文件上传固定的路径
			StringBuffer relativePath = new StringBuffer(cpjsProperties.getSavePath());
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String zzsj = sdf.format(date);
			String suffixName = fileName.substring(fileName.lastIndexOf("."));
			//pdf重命名eg. yyzz20181031152840.pdf
			String fileName_new = "yyzz" + zzsj + suffixName;
			StringBuffer new_folder = new StringBuffer();
			new_folder = new StringBuffer(vo.getQyid()).append("/");
			String folderName = relativePath.append(new_folder).toString();
			//创建文件夹
			File dir = new File(folderName);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			//文件全路径
			StringBuffer allPath = new StringBuffer(folderName).append(fileName_new);
			// 加完水印的文件
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(allPath.toString()));
			int total = reader.getNumberOfPages() + 1;
			PdfContentByte content;
			// 设置字体
			BaseFont base = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",
					BaseFont.EMBEDDED);
			String waterText = "仅供十八届消防展审核";
			int j = waterText.length(); // 文字长度
			char c = 0;
			com.itextpdf.text.Rectangle pageSize = reader.getPageSize(1);
			float height = pageSize.getHeight();
			float width = pageSize.getWidth();
			float high = 0;// 高度
			// 循环对每页插入水印
			for (int i = 1; i < total; i++) {
				// 水印的起始
				high = height/2;
				content = stamper.getUnderContent(i);
				// 开始
				content.beginText();
				// 设置颜色
				BaseColor color = new BaseColor(105, 105, 105);
				content.setColorFill(color);
				// 设置字体及字号
				content.setFontAndSize(base, width/11);
				// 设置起始位置
				content.setTextMatrix(0, height/2);
				PdfGState gs = new PdfGState();
				float alpha = 0.5f;
				gs.setFillOpacity(alpha);
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
			String fileAddress = allPath.toString();
			String savePath = folderName+"/"+"yyzz" + zzsj;
			((QyjbxxService) AopContext.currentProxy()).pdf2image(fileAddress,savePath,"jpg");
			String dbPath = new_folder + "yyzz" + zzsj +".jpg";
			vo.setSrc(dbPath);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return vo;
	}
	@Override
	public String pdf2image(String fileAddress,String savePath,String type) {
		// 将pdf装图片 并且自定义图片得格式大小
		File file = new File(fileAddress);
		try {
			PDDocument doc = PDDocument.load(file);
			PDFRenderer renderer = new PDFRenderer(doc);
			//int pageCount = doc.getNumberOfPages();
			for (int i = 0; i < 1; i++) {//只转pdf的第一页
				BufferedImage image = renderer.renderImageWithDPI(i, 144); // Windows native DPI
				Graphics2D g2 = image.createGraphics();
				g2.drawImage(image, 0, 0, image.getWidth(null), image.getHeight(null), null);
				//颜色
				Color color = new Color(105, 105, 105);
				g2.setColor(color);
				//字体大小为图片宽的1/11
				g2.setFont(new Font("宋体", Font.BOLD, image.getWidth(null) / 11));
				//透明度
				float alpha = 0.5f;
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,alpha));
				String text = "仅供十八届消防展审核";
				g2.drawString(text, 0, image.getHeight(null) / 2);
				g2.dispose();
				//A.写到文件夹
				FileOutputStream out = new FileOutputStream(savePath+"."+type);
				ImageIO.write(image, type, out);
				out.close();
			}
			doc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	//统计分析查询是否信息确认  add by rliu 2019/1/4 10:35
	public List<QyjbxxVO> ifConfirmedTjfx(QyjbxxVO qyjbxxVO){
		return qyjbxxDAO.ifConfirmedTjfx(qyjbxxVO);
	}

	//统计分析查询是否信息确认_详情  add by rliu 2019/1/7 10:35
	public List<QyjbxxVO> ifConfirmedTjfxDetail(QyjbxxVO qyjbxxVO){
		return qyjbxxDAO.ifConfirmedTjfxDetail(qyjbxxVO);
	}
}