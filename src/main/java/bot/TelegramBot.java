package bot;


import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.TrackLink;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.hc.core5.http.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private static final String clientId = "5d68dc795c8f4b138341afa848dddffe";
    private static final String clientSecret = "7872f86638ce4ddd9110375a259475a2";
    private static final String accessToken = "BQBj-A4d0LobkVLYwjJeR1VTC6bEIZ16VcF6yBaVarqgnMRtJrjWmJO12wdJh6LdFh8kJk9yNnva1Jc1OvM";

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .build();
    private static final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
            .build();

    public synchronized void onUpdateReceived(Update update) {
        // Example: Echo bot
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            String searchText = update.getMessage().getText();
//            String sendText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if (searchText.equals("/start")) {
                SendMessage message = new SendMessage()
                        .setChatId(chatId)
                        .setText("Введіть виконавця або назву пісні");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            } else {
                try {
                    String Url = "https://soundcloud.com/search/sounds?q=" + URLEncoder.encode(searchText, "UTF-8");
                    Document doc = Jsoup
                            .connect(Url)
                            .header("Accept-Encoding", "gzip, deflate")
                            .userAgent("Mozilla/5.0 (Linux; Android 4.1.1; Nexus 7 Build/JRO03D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Safari/535.19")
//                            .maxBodySize(0)
                            .timeout(6000)
                            .followRedirects(true)
                            .get();
                    // For all requests an access token is needed

                    final ClientCredentials clientCredentials = clientCredentialsRequest.execute();

                    // Set access token for further "spotifyApi" object usage
                    spotifyApi.setAccessToken(clientCredentials.getAccessToken());

                    SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(searchText).build();
                    GetTrackRequest getTrackRequest = spotifyApi.getTrack(clientId).build();
                    Paging<Track> tracks = searchTracksRequest.execute();
                    Track track = getTrackRequest.execute();
                    SendMessage message = new SendMessage()
                            .setChatId(chatId)
                            .setText("Found " + tracks + " tracks for " + searchText)
                            .setText("Your track " + track.getHref());
                    execute(message);

//                    Elements resultOfSearch = doc.getElementsByClass("search-page__tracks");
//                    Elements elements = resultOfSearch.select("div[data-dkey]");
//                    for (Element element : elements) {
//                        String artName = element.select(".musicset-track__artist").text();
//                        String trackName = element.select(".musicset-track__track-name").text();
//
//                        SendMessage message = new SendMessage()
//                                .setChatId(chatId)
//                                .setText(artName + " " + trackName);
//
//                        try {
//                            execute(message);
//                        } catch (TelegramApiException e) {
//                            e.printStackTrace();
//                        }


                    } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (SpotifyWebApiException e) {
                    e.printStackTrace();
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getBotUsername() {
        return "music_bot";
    }

    public String getBotToken() {
        return "1276165749:AAFV-am-ST3PNl3xJ-Ci5s1fvKo7nDEAJuU";
    }
}

