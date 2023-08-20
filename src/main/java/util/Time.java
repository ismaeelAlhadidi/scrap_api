package util;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;

public class Time {
	static int[] daysOfMonth = new int[] {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
	static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static String[] arabicWeekDays = new String[] {
			"الأحد", 
			"الاثنين", 
			"الثلاثاء", 
			"الأربعاء", 
			"الخميس", 
			"الجمعة", 
			"السبت"
	};
	static String[] arabicMonths = new String[] {
			"يناير" + " / " + "كانون الثاني", 
			"فبراير" + " / " + "شباط", 
			"مارس" + " / " + "آذار", 
			"أبريل" + " / " + "نيسان", 
			"مايو" + " / " + "أيار", 
			"يونيو" + " / " + "حزيران", 
			"يوليو" + " / " + "تموز", 
			"أغسطس" + " / " + "آب", 
			"سبتمبر" + " / " + "أيلول", 
			"أكتوبر" + " / " + "تشرين الأول",
			"نوفمبر" + " / " + "تشرين الثاني",  
			"ديسمبر" + " / " + "كانون الأول", 
	};
	static String[] arabicNumbers = new String[] {
		"٠",
		"١",
		"٢",
		"٣",
		"٤",
		"٥",
		"٦",
		"٧",
		"٨",
		"٩"
	};
 	public static class DateTime {
		int hour, minute, second;
		int year, month, day;
	}
 	public static class Date {
 		public int day, month, year;
 	}
	public static boolean isBeforeLessThan1Hour(String t) {
		long now = System.currentTimeMillis();
		long dateTime = getTimeInMillis(t);
		long[] dp = new long[7];
		dp[0] = 1000;
		dp[1] = 60;
		dp[2] = 60;
		dp[3] = 24;
		dp[4] = 7;
		dp[5] = 4;
		dp[6] = 12;
		for(int i = 1; i < dp.length; ++i) {
			dp[i] = dp[i] * dp[i-1];
		}
		dateTime = now-dateTime;
		for(int i = 6; i > 0; --i) {
			if(dateTime/dp[i] > 0) {
				if(6-i < 5) {
					return false;
				}
				return true;
			}
		}
		return true;
	}
	public static int getDaysOfMonth(int m, boolean isLeap) {
		if(m == 2 && isLeap) {
			return 29;
		}
		return daysOfMonth[m-1];
	}
	public static boolean isLeap(int year) {
		return (year%4 == 0);
	}
	public static boolean atLastTwoDays(String t) {
		Date date = convertStringToDate(t);
		Date today = convertStringToDate(convertToString(System.currentTimeMillis()));
		if(compare(date, today) == 0 || compare(date, prevDay(today)) == 0) {
			return true;
		}
		return false;
	}
	public static Date convertStringToDate(String t) {
		char[] s = t.toCharArray();
		int[] d = new int[3];
		int pointer = 0;
		int number = 0;
		for(int i = 0; i < s.length; ++i) {
			if(s[i] == ' ') break;
			if(s[i] == '-') {
				d[pointer++] = number;
				number = 0;
				if(pointer >= d.length) break; 
			} else {
				number = (number*10) + (s[i]-'0');
			}
		}
		d[pointer++] = number;
		Date date = new Date();
		date.year = d[0];
		date.month = d[1];
		date.day = d[2];
		return date;
	}
	public static Date prevDay(Date date) {
		Date prev = new Date();
		if(date.day > 1) {
			prev.year = date.year;
			prev.month = date.month;
			prev.day = date.day-1;
			return prev;
		}
		if(date.month > 1) {
			prev.year = date.year;
			prev.month = date.month-1;
			prev.day = getDaysOfMonth(prev.month, isLeap(prev.year));
			return prev;
		}
		prev.year = date.year-1;
		prev.month = 12;
		prev.day = getDaysOfMonth(12, isLeap(prev.year));
		return prev;
	}
	public static int compare(Date d1, Date d2) {
		if(d1.year != d2.year) {
			return d1.year > d2.year ? 1 : -1;
		}
		if(d1.month != d2.month) {
			return d1.month > d2.month ? 1 : -1;
		}
		if(d1.day == d2.day) return 0;
		return d1.day > d2.day ? 1 : -1;
	}
	public static String convertToString(long millis) {
		return formatter.format(millis);
	}
	public static String readableForamt(String t) {
		DateTime dateTime = convertToDateTime(t);
		//int dayOfWeek = getDayNumberOld(new java.util.Date(dateTime.year, dateTime.month, dateTime.day))-1;
		int dayOfWeek = getDayOfWeek(LocalDate.of(dateTime.year, dateTime.month, dateTime.day)) % 7;
		StringBuilder result = new StringBuilder();
		result.append(arabicWeekDays[dayOfWeek]);
		result.append("،");
		result.append(" ");
		result.append(convertToArabicNumber(dateTime.day));
		result.append(" ");
		result.append(arabicMonths[dateTime.month-1]);
		result.append(" ");
		result.append(convertToArabicNumber(dateTime.year));
		return new String(result);
	}
	public static long getTimeInMillis(String t) {
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(t));
		} catch(Exception e) {};
		return calendar.getTimeInMillis();
	}
	public static String beforeFormat(String t) {
		long now = System.currentTimeMillis();
		long dateTime = getTimeInMillis(t);
		long[] dp = new long[7];
		dp[0] = 1000;
		dp[1] = 60;
		dp[2] = 60;
		dp[3] = 24;
		dp[4] = 7;
		dp[5] = 4;
		dp[6] = 12;
		for(int i = 1; i < dp.length; ++i) {
			dp[i] = dp[i] * dp[i-1];
		}
		dateTime = now-dateTime;
		String[][] names = new String[][] {
			{"سنة", "سنتان", "سنوات"},
			{"شهر", "شهران", "شهور"},
			{"أسبوع", "أسبوعان", "أسابيع"},
			{"يوم", "يومان", "أيام"},
			{"ساعة", "ساعتان", "ساعات"},
			{"دقيقة", "دقيقتان", "دقائق"}
		};
		String v = "منذ" + " ";
		long temp;
		for(int i = 6; i > 0; --i) {
			if(dateTime/dp[i] > 0) {
				temp = dateTime/dp[i];
				if(temp == 1) {
					v += names[6-i][0];
				} else if(temp == 2) {
					v += names[6-i][1];
				} else if(temp < 11) {
					v += temp + " " + names[6-i][2];
				} else {
					v += temp + " " + names[6-i][0];
				}
				return v;
			}
		}
		return "الآن";
	}
	public static String beforeFormat2(String t) {
		DateTime now = convertToDateTime(System.currentTimeMillis());
		DateTime dateTime = convertToDateTime(t);
		String v = "منذ" + " ";
		if (dateTime.year < now.year) {
			int n = now.year - dateTime.year;
			if (n == 1)
				return v + "سنة";
			if (n == 2)
				return v + "سنتان";
			if (n < 11) 
				return v + n + " " + "سنوات";
			return v + n + " " + "سنة";
		} else if (dateTime.month < now.month) {
			int n = now.month - dateTime.month;
			if (n == 1)
				return v + "شهر";
			if (n == 2)
				return v + "شهران";
			if (n < 11) 
				return v + n + " " + "شهور";
			
			return v + n + " " + "شهر";
		} else if (dateTime.day < now.day) {
			int n = now.day - dateTime.day;
			if (n == 1)
				return v + "يوم";
			if (n == 2)
				return v + "يومان";
			if (n < 7) 
				return v + n + " " + "أيام";
			n = n/7;
			if (n == 1) 
				return v + "أسبوع";
			if (n == 2)
				return v + "أسبوعان";
			
			return v + n + " " + "أسابيع";
		} else if (dateTime.hour < now.hour) {
			int n = now.hour - dateTime.hour;
			if (n == 1)
				return v + "ساعة";
			if (n == 2)
				return v + "ساعتان";
			if (n < 11) 
				return v + n + " " + "ساعات";
			
			return v + n + " " + "ساعة";
		} else if (dateTime.minute < now.minute) {
			int n = now.minute - dateTime.minute;
			if (n == 1)
				return v + "دقيقة";
			if (n == 2)
				return v + "دقيقتان";
			if (n < 11) 
				return v + n + " " + "دقائق";
			
			return v + n + " " + "دقيقة";
		}
		return "الآن";
	}
	
	public static DateTime convertToDateTime(long millis) {
		return convertToDateTime(formatter.format(millis));
	}
	
	public static DateTime convertToDateTime(String t) {
		DateTime result = new DateTime();
		String[] a = t.split(" ");
		char[] time = a[1].toCharArray();
		char[] date = a[0].toCharArray();
		char[] inHand = time;
		int[] temp1 = new int[3], temp2 = new int[3], temp3 = temp1;
		int pointer, number;
		char sp1 = ':', sp2 = '.';
		for (int j = 0; j < 2; ++j) {
			pointer = 0;
			number = 0;
			for (int i = 0; i < inHand.length; ++i) {
				if (inHand[i] == sp1 || inHand[i] == sp2) {
					temp3[pointer++] = number;
					number = 0;
				} else {
					number = number * 10 + (inHand[i] - '0');
					if(i == inHand.length-1) {
						temp3[pointer++] = number;
					}
				}
				if (pointer >= temp3.length)
					break;
			}
			sp1 = '-';
			inHand = date;
			temp3 = temp2;
		}
		result.hour = temp1[0];
		result.minute = temp1[1];
		result.second = temp1[2];
		result.year = temp2[0];
		result.month = temp2[1];
		result.day = temp2[2];
		return result;
	}
	public static int getDayNumberOld(java.util.Date date) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    return cal.get(Calendar.DAY_OF_WEEK);
	}
	public static int getDayOfWeek(LocalDate date) {
		DayOfWeek day = date.getDayOfWeek();
	    return day.getValue();
	}
	public static String convertToArabicNumber(int n) {
		List<String> digits = new ArrayList<String>();
		while(n > 0) {
			digits.add(arabicNumbers[n%10]);
			n /= 10;
		}
		reverse(digits);
		StringBuilder number = new StringBuilder();
		for(String digit : digits) {
			number.append(digit);
		}
		return new String(number);
	}
	public static void reverse(List<String> list) {
		int left = 0, right = list.size()-1;
		String temp;
		while(left < right) {
			temp = list.get(left);
			list.set(left,  list.get(right));
			list.set(right, temp);
			++left;
			--right;
		}
	}
}
