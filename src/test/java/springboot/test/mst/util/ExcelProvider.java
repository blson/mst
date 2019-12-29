package springboot.test.mst.util;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.lang.reflect.Method;
import java.util.*;


public class ExcelProvider implements Iterator<Object[]>{
	private Logger log = LogManager.getLogger(this.getClass());
	private Workbook workBook = null;
	private Sheet sheet = null;
	private int rowNum = 0;
	private int curRowNo = 0;
	private int columnNum = 0;
	private String[] columnName;
	private int singleLine = 0;


	private Method locateTestMethod(Object obj, String methodName) {
		try {
			Method[] arrayOfMethod1 = obj.getClass().getDeclaredMethods();
			Method[] arrayOfMethod2 = arrayOfMethod1;
			int j = arrayOfMethod2.length;
			int k = 0;
			while (k < j) {
				Method localMethod = arrayOfMethod2[k];

				if (localMethod.getName().equals(methodName)) {
					return localMethod;
				}
				++k;
			}
		} catch (Throwable ex) {
			log.error("Incorrect method name：{}", ex);
		}
		return null;
	}

	public String[] getFileName(String path) {
		File file = new File(path);
		String[] fileName = file.list();
		return fileName;
	}

	public ExcelProvider(Object obj, String aimmathod, String envParameter) {
		try {
			//excel名称获取
			String excelNameString = obj.getClass().toString().contains("java.lang.String") ? obj.toString() : obj.getClass().getName();
			File excelfile = null;
			String path = new File("./").getCanonicalPath() + "/src/main/resources/datadriver/" + envParameter + "/excel/"
					+ excelNameString.replaceAll("\\.", "/") + ".xls";
			path = path.replace("/target", "").replace("\\target", "");
			excelfile = new File(path);
			String[] envs = getFileName(new File("./").getCanonicalPath().replace("/target", "").replace("\\target", "")
					+ "/src/main/resources/datadriver/");
			if (!excelfile.exists()) {
				for (String env : envs) {
					path = new File("./").getCanonicalPath() + "/src/main/resources/datadriver/" + env + "/excel/"
							+ excelNameString.replaceAll("\\.", "/") + ".xls";
					path = path.replace("/target", "").replace("\\target", "");
					excelfile = new File(path);
					if (excelfile.exists()) {
						break;
					}
				}
			}
			log.info("Current excel path:{}" + path);
			this.workBook = Workbook.getWorkbook(excelfile);
			this.sheet = workBook.getSheet(aimmathod);
			this.rowNum = sheet.getRows();
			// 得到第一行数据，将第一行的数据作为map的key
			Cell[] c = sheet.getRow(0);
			this.columnNum = c.length;
			columnName = new String[c.length];
			for (int i = 0; i < c.length; i++) {
				columnName[i] = c[i].getContents().toString();
			}

			try {
				DP dpr = locateTestMethod(obj, aimmathod).getAnnotation(DP.class);
				if (dpr != null) {
					this.singleLine = dpr.includeThisRow();
				}
			} catch (NullPointerException ex) {
				throw new RuntimeException("sheetName与methedName要一致，否则注解无法使用");
			}
			if (this.singleLine > 0)
				this.curRowNo = this.singleLine;
			else
				this.curRowNo++;
		} catch (Exception ex) {
			log.error("Initialize the file exception：{}", ex);
		}

	}

	@Override
	public boolean hasNext() {
		if (this.rowNum == 0 || this.curRowNo >= this.rowNum) {
			workBook.close();
			return false;
		}
		if (this.singleLine > 0 && this.curRowNo > this.singleLine) {
			workBook.close();
			return false;
		}
		return true;
	}

	@Override
	public Object[] next() {
		Cell[] c = sheet.getRow(this.curRowNo);
		Map<String, Object> s = new LinkedHashMap<String, Object>();

		for (int i = 0; i < this.columnNum; i++) {
			String data = c[i].getContents().toString();
			if (data.trim().endsWith("}}]]") && data.trim().startsWith("[[{{"))
				s.put(this.columnName[i], getSubTable(data.trim()));
			else
				s.put(this.columnName[i], data);
		}
		this.curRowNo++;
		return new Object[] { s };
	}
	
	@SuppressWarnings("unchecked")
	private List<?> getSubTable(String key) {
		List<Object> list = new LinkedList<>();
		String classname = "";
		String sheetname = "";
		String id = "";
		try {
			String str = key.substring(4, key.length() - 4);

			for (String s : str.split(",")) {
				String[] keyValueStrings = s.split(":");
				if (keyValueStrings.length == 2) {
					int beginF = keyValueStrings[0].indexOf("\"") + 1;
					int endF = keyValueStrings[0].lastIndexOf("\"");
					if (endF > beginF) {
						String subkey = keyValueStrings[0].substring(beginF, endF);
						beginF = keyValueStrings[1].indexOf("\"") + 1;
						endF = keyValueStrings[1].lastIndexOf("\"");
						if (subkey.equalsIgnoreCase("classname") && endF > beginF) {
							classname = keyValueStrings[1].substring(beginF, endF);
						}
						if (subkey.equalsIgnoreCase("sheetname") && endF > beginF) {
							sheetname = keyValueStrings[1].substring(beginF, endF);
						}
						if (subkey.equalsIgnoreCase("id") && endF > beginF) {
							id = keyValueStrings[1].substring(beginF, endF);
						}
					}
				}

			}
			if (sheetname.length() < 1) {
				throw new Exception("can not get sheetname");
			}
			if (sheetname.length() < 1) {
				throw new Exception("can not get id");
			}

		} catch (Exception ex) {
			log.error("unknown exception：{}", ex);
			return list;
		}

		SubTableProvider subTableProvider = new SubTableProvider(workBook.getSheet(sheetname));

		while (subTableProvider.hasNext()) {
			Object[] d = subTableProvider.next();
			if (d[0].toString().trim().equals(id)) {
				if (classname == null || classname.trim().length() == 0) {
					list.addAll((List<Map<String, String>>) d[1]);
				} else {
					for (Map<String, String> tempMap : (List<Map<String, String>>) d[1]) {
						Object bean;
						try {
							bean = (Class.forName(classname)).newInstance();
							BeanUtils.populate(bean, tempMap);
							list.add(bean);
						} catch (Exception ex) {
							return list;
						}
					}
				}
				break;
			}
		}
		return list;
	}	
	
	class SubTableProvider implements Iterator<Object[]> {

		private Sheet subSheet = null;
		private int subSheetRowNum = 0;
		private int curPhysicalRowNo = 0;
		private final String[] subSheetColumnName;

		public SubTableProvider(Sheet sheet) {
			this.subSheet = sheet;
			this.subSheetRowNum = sheet.getRows();
			Cell[] c = sheet.getRow(0);
			subSheetColumnName = new String[c.length];
			for (int i = 0; i < c.length; i++) {
				subSheetColumnName[i] = c[i].getContents().toString().replace("\n", "");
			}
			this.curPhysicalRowNo++;
		}

		@Override
		public boolean hasNext() {
			if (this.subSheetRowNum == 0 || this.curPhysicalRowNo >= this.subSheetRowNum) {
				return false;
			}
			return true;
		}

		@Override
		public Object[] next() {
			Object r[] = new Object[2];
			List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
			int RangeRow = 1;
			boolean thisIsRange = false;
			jxl.Range[] ranges = subSheet.getMergedCells();
			// 首先要确定第一列占了几行，要区分 Range 还是 Row
			for (jxl.Range space : ranges) {
				if (space.getTopLeft().getColumn() == 0 && space.getBottomRight().getColumn() == 0
						&& space.getTopLeft().getRow() == this.curPhysicalRowNo) {
					RangeRow = space.getBottomRight().getRow() - space.getTopLeft().getRow() + 1;
					thisIsRange = true;
					break;
				}
			}
			// 获取第一列的值
			String key = "";
			if (thisIsRange) {
				key = subSheet.getRow(this.curPhysicalRowNo)[0].getContents().toString();
			} else {
				key = subSheet.getRow(this.curPhysicalRowNo)[0].getContents().toString();
			}
			// 从第二列开始转换为list<map>
			if (thisIsRange) {
				for (int j = 0; j < RangeRow; j++) {
					Map<String, String> tempMap = new HashMap<>();
					for (int i = 1; i < this.subSheetColumnName.length; i++) {
						tempMap.put(this.subSheetColumnName[i], subSheet.getRow(this.curPhysicalRowNo + j)[i].getContents().toString());
					}
					list.add((HashMap<String, String>) tempMap);
				}
			} else {
				Map<String, String> tempMap = new HashMap<>();
				for (int i = 1; i < this.subSheetColumnName.length; i++) {
					tempMap.put(this.subSheetColumnName[i], subSheet.getRow(this.curPhysicalRowNo)[i].getContents().toString());
				}
				list.add((HashMap<String, String>) tempMap);
			}
			r[0] = key;
			r[1] = list;
			this.curPhysicalRowNo = this.curPhysicalRowNo + RangeRow;
			return r;
		}
	}
}
