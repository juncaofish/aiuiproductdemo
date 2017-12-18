package com.iflytek.aiuiproduct.player;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TtsTextReplacer {

	public static String replace(String text) {
		String str = replaceDate(text);
		str = replacePersonName(str);
		str = replaceTemperature(str);
		str = replaceSpecial(str);
		str = replacePunctuation(str);
		return str;
	}
	
	private static String replacePunctuation(String text) {
//		String regex = "[_\\-\\*\\.]";
		String regex = "[_\\*]";
		return replace(text, regex, "", "");
	}
	
	private static String replaceDate(String text) {
		return text;
	}

	private static String replacePersonName(String text) {
		String str = text.replaceAll("朴树", "普树");
		str = str.replaceAll("单田芳", "善田芳");
		return str;
	}

	private static String replaceTemperature(String text) {
		String regex = "\\-\\d+℃|\\-\\d+℉|\\-\\d+[摄华攝華]氏度";
		return replace(text, regex, "-", "零下");
	}

	private static String replaceSpecial(String text) {
		String str = replace(text, "[动信通]\\d[gG]", "(?i)g", "记");
		str = replace(str, "\\d[gG][\u4e00-\u9fa5]{0,3}网", "(?i)g", "记");
		str = replace(str, "网[\u4e00-\u9fa5]{0,3}\\d[gG]", "(?i)g", "记");
		str = replace(str, "\\D[(2.4)|5][gG]", "(?i)g", "记");
		str = replace(str, "(^|\\D)\\d0后", "0", "零");
		str = replace(str, "(^|\\D)58同城", "5", "五");
		str = replace(str, "(^|[^a-zA-Z])(?i)tfboy", "(?i)tf", " t f ");
		if (!isOnlyDate(str)) {
			str = addSeparate(str, "(^|\\D)20\\d{2}");
		}
		str = addSeparate(str, "(^|\\D)\\d{4}年");
		str = str.replaceAll("123木头人", "一二三木头人");
		str = str.replaceAll("塞下", "鳃下");
		str = str.replaceAll("黄梅调", "黄梅吊");
		str = str.replaceAll("把家还", "把家环");
		str = str.replaceAll("北京长书广播", "北京常书广播");
		str = str.replaceAll("1047私家车", "1 0 4 7私家车");
		return str;
	}
	
	private static boolean isOnlyDate(String str) {
		return str.matches("\\d{4}-\\d{1,2}-\\d{1,2}");
	}
	
	private static String replace(String text, String regex, String regularExpression, String replacement) {
		try {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(text);
			StringBuffer sb = new StringBuffer();
			String[] regulars = regularExpression.split("\\|");
			while (matcher.find()) {
				matcher.appendReplacement(sb, "");
				String matchTxt = text.substring(matcher.start(), matcher.end());
				if (regulars != null && regulars.length > 0) {
					for (String regular: regulars) {
						if (regular != null && regular.length() > 0) {
							matchTxt = matchTxt.replaceAll(regular, replacement);
						} else matchTxt = "";
					}
				} else {
					matchTxt = "";
				}
				sb.append(matchTxt);
			}
			matcher.appendTail(sb);
			return sb.toString();
		} catch (Exception e) {
			return text;
		}
	}
	
	public static String replace(String text, String regex, String replacement) {
		try {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(text);
			StringBuffer sb = new StringBuffer();
			while (matcher.find()) {
				matcher.appendReplacement(sb, "");
				String matchTxt = text.substring(matcher.start(), matcher.end());
				matchTxt = matchTxt.replaceAll(matchTxt, replacement);
				sb.append(matchTxt);
			}
			matcher.appendTail(sb);
			return sb.toString();
		} catch (Exception e) {
			return text;
		}
	}

	public static String addSeparate(String text, String regex) {
		try {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(text);
			StringBuffer sb = new StringBuffer();
			while (matcher.find()) {
				matcher.appendReplacement(sb, "");
				String matchTxt = text.substring(matcher.start(), matcher.end());
				StringBuilder sb2 = new StringBuilder();
				char[] chrs = matchTxt.toCharArray();
				if (chrs != null) {
					for (char c : chrs) {
						sb2.append(c);
						sb2.append(' ');
					}
				}
				matchTxt = sb2.toString();
				sb.append(matchTxt);
			}
			matcher.appendTail(sb);
			return sb.toString();
		} catch (Exception e) {
			return text;
		}
	}
}
