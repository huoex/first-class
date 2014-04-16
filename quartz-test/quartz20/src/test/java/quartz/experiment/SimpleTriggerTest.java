package quartz.experiment;

import static org.quartz.CalendarIntervalScheduleBuilder.calendarIntervalSchedule;
import static org.quartz.DateBuilder.dateOf;
import static org.quartz.SimpleScheduleBuilder.repeatHourlyForever;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerUtils;
import org.quartz.spi.OperableTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Comment for SimpleTriggerTest.
 * 
 * @author Zemian Deng
 */
public class SimpleTriggerTest {
	private static Logger logger = LoggerFactory
			.getLogger(SimpleTriggerTest.class);

	@Test
	public void testStartTime() throws Exception {
		Date startTime = dateOf(0, 0, 0, 1, 1, 2011);
		SimpleTrigger trigger = newTrigger().withIdentity("test")
				.withSchedule(repeatHourlyForever()).startAt(startTime).build();
		List<Date> fireTimes = TriggerUtils.computeFireTimes(
				(OperableTrigger) trigger, null, 48);
		for (Date fireTime : fireTimes)
			System.out.println(fireTime);
	}

	@Test
	public void testShowStartingTime() throws Exception {
		logger.info("Current time: " + new Date());
		showTriggerFireTimes(simpleTrigger(-1, 5000), new Date(), 10);
	}

	private void showTriggerFireTimes(SimpleTrigger simpleTrigger,
			Date startTime, int maxCount) {
		logger.info("simpleTrigger repeat=" + simpleTrigger.getRepeatCount()
				+ ", interval=" + simpleTrigger.getRepeatInterval());
		Date nextDate = startTime;
		int i = 0;
		while (i++ < maxCount) {
			Date fireTime = simpleTrigger.getFireTimeAfter(nextDate);
			logger.info("Next fireTime " + fireTime);
			nextDate = fireTime;
		}
	}

	private SimpleTrigger simpleTrigger(int repeatCount, long repeatInterval) {
		return TriggerBuilder
				.newTrigger()
				.withIdentity("test")
				.withSchedule(
						SimpleScheduleBuilder.simpleSchedule()
								.withRepeatCount(repeatCount)
								.withIntervalInMilliseconds(repeatInterval))
				.build();
	}

	@Test
	public void testEvery3Days() throws Exception {
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm")
				.parse("2011-07-05 18:30");
		Trigger trigger = newTrigger().withIdentity("test").startAt(date)
				.withSchedule(calendarIntervalSchedule().withIntervalInDays(3))
				.build();

		logger.info("Trigger type " + trigger.getClass());
		List<Date> dates = TriggerHelper.getNextFireTimes(trigger, 20);
		for (Date dt : dates)
			logger.info("NextFireTime " + dt);
	}
}
