package bg.swu.example.iot.leshan.server;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.server.LeshanServer;
import org.eclipse.leshan.server.registration.Registration;

public class PollingTask {
	public static final int TEMPERATURE_SENSOR_ID = 3303;
	/** The ID of the resource providing the temperature value. */
	public static final int TEMPERATURE_RESOURCE_ID = 5700;

	private final LeshanServer server; // Assuming you have your server instance

	public PollingTask(LeshanServer server) {
		this.server = server;
	}

	// Start polling every 5 seconds
	public void startPolling() {
		Timer timer = new Timer(true);
		// Every 5 seconds
		timer.scheduleAtFixedRate(
			new TimerTask() {
				@Override
				public void run() {
					server.getRegistrationService().getAllRegistrations().forEachRemaining(
						registration -> printTemperatureValue(registration)
					);
				}
			},
			0,
			5000
		);
	}

	private String getTemperatureValue(Registration registration) {
		// Send ReadRequest for temperature resource (ID 5700)
		final ReadRequest request = new ReadRequest(
			TEMPERATURE_SENSOR_ID, 0, TEMPERATURE_RESOURCE_ID
		);
		try {
			final ReadResponse response = server.send(registration, request);
			if (response.isSuccess()) {
				return Optional.ofNullable(response.getContent())
					.filter(LwM2mResource.class::isInstance)
					.map(LwM2mResource.class::cast)
					.map(LwM2mResource::getValue)
					.map(Object::toString)
					.orElse(null);
			} else {
				System.out.println(
					registration.getEndpoint() +
					": ❌ Failed to read temperature: " + response.getCode()
				);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	private void printTemperatureValue(Registration registration) {
		Optional.ofNullable(getTemperatureValue(registration)).ifPresent(
			temperature -> printTemperatureValue(registration.getEndpoint(), temperature)
		);
	}

	private void printTemperatureValue(String endpoint, String temperature) {
		System.out.println(endpoint + ": 🌡 Temperature: " + temperature + "°C");
	}
}
