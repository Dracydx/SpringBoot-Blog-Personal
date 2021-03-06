package cn.edu.jxnu.blog.controller.admin;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.edu.jxnu.blog.domin.BlogType;
import cn.edu.jxnu.blog.domin.PageBean;
import cn.edu.jxnu.blog.service.BlogService;
import cn.edu.jxnu.blog.service.BlogTypeService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @Description 博客分类控制器
 */
@RestController
@RequestMapping(value = "/admin/blogType")
public class BlogTypeAdminController {


	private static final Logger log = org.slf4j.LoggerFactory
			.getLogger(BlogTypeAdminController.class);
	@Resource
	private BlogTypeService blogTypeService;
	@Autowired
	private BlogService blogService;

	// 分页查询博客类别
	@RequestMapping("/list")
	public String listBlogType(
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "limit", required = false) String limit)
			throws Exception {
		log.info("当前请求博客类型管理页面。。。");
		// 定义分页bean
		PageBean<BlogType> pageBean = new PageBean<BlogType>(
				Integer.parseInt(page), Integer.parseInt(limit));
		// 拿到分页结果已经记录总数的pageBean
		pageBean = blogTypeService.listByPage(pageBean);
		// 使用阿里巴巴的fastJson创建JSONObject
		JSONObject result = new JSONObject();
		// 通过fastJson序列化list为jsonArray
		String jsonArray = JSON.toJSONString(pageBean.getResult());
		JSONArray array = JSONArray.parseArray(jsonArray);
		// 将序列化结果放入json对象中
		result.put("data", array);
		result.put("code", 0);
		result.put("count", pageBean.getTotal());

		// 使用自定义工具类向response中写入数据
		return result.toJSONString();
	}

	// 添加和更新博客类别
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String save(BlogType blogType, HttpServletRequest httpServletRequest)
			throws Exception {
		log.info("当前更新博客类别。。。");
		int resultTotal = 0; // 接收返回结果记录数
		if (blogType.getId() == null) { // 说明是第一次插入
			resultTotal = blogTypeService.addBlogType(blogType);
		} else { // 有id表示修改
			resultTotal = blogTypeService.updateBlogType(blogType);
		}

		JSONObject result = new JSONObject();
		if (resultTotal > 0) {
			result.put("success", true);
		} else {
			result.put("success", false);
		}
		// 在这里刷新application
		ServletContext application = httpServletRequest.getServletContext();
		application.removeAttribute("blogTypeList");// 清空ServletContext，触发ServletContextAttributeListener
		return result.toJSONString();
	}

	// 博客类别信息删除
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public String deleteBlog(
			@RequestParam(value = "ids", required = false) String ids,
			HttpServletRequest httpServletRequest) throws Exception {
		// 分割字符串得到id数组
		log.info("当前请求删除博客类别。。。");
		String[] idsStr = ids.split(",");
		JSONObject result = new JSONObject();
		for (int i = 0; i < idsStr.length; i++) {
			int id = Integer.parseInt(idsStr[i]);
			// 要判断该博客类别下面是否有博客 如果有就禁止删除博客类别 ，等我们完成博客对应的操作再来完善
			if (blogService.getBlogByTypeId(id) > 0) {
				result.put("success", 1);
				return result.toJSONString();
			} else {
				blogTypeService.deleteBlogType(id);
			}
		}
		result.put("success", 0);// 返回一个json对象，带有一个boolean值，写入输出流response中。
		ServletContext application = httpServletRequest.getServletContext();
		application.removeAttribute("blogTypeList");// 清空ServletContext，触发ServletContextAttributeListener
		return result.toJSONString();
	}
}