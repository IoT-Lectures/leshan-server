package bg.swu.example.iot.leshan.server;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.leshan.core.model.InvalidDDFFileException;
import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.server.endpoint.LwM2mServerEndpointsProvider;
import org.eclipse.leshan.server.model.LwM2mModelProvider;
import org.eclipse.leshan.server.model.VersionedModelProvider;
import org.eclipse.leshan.transport.javacoap.server.endpoint.JavaCoapServerEndpointsProvider;

public class ServerApp {
	public static void main(String[] args) {
		final String host = "localhost";
		final int port = 5683;

		// We obtain the xml files from https://github.com/OpenMobileAlliance/lwm2m-registry
		final List<ObjectModel> models = Stream.concat(
			ObjectLoader.loadDefault().stream(),
			Stream.of("/models/3.xml", "/models/3303.xml").flatMap(
			file -> {
					try {
						return ObjectLoader.loadDdfFile(
							ServerApp.class.getResourceAsStream(file), file
						).stream();
					} catch (InvalidDDFFileException e) {
						throw new RuntimeException(e);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
			)
		).collect(Collectors.toList());

		final LwM2mModelProvider modelProvider = new VersionedModelProvider(models);

		final List<LwM2mServerEndpointsProvider> endpointsProviders = new ArrayList<>();
		final InetSocketAddress address = new InetSocketAddress(host, port);
		endpointsProviders.add(new JavaCoapServerEndpointsProvider(address));
	}
}

