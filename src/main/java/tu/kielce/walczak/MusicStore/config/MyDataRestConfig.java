package tu.kielce.walczak.MusicStore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import tu.kielce.walczak.MusicStore.entity.Album;
import tu.kielce.walczak.MusicStore.entity.Artist;
import tu.kielce.walczak.MusicStore.entity.Genre;
import tu.kielce.walczak.MusicStore.entity.Subgenre;

@Configuration
public class MyDataRestConfig implements RepositoryRestConfigurer {

    @Value("${allowed.origins}")
    private String[] allowedOrigins;

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {

        HttpMethod[] unsupportedActions = {HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.PATCH};
        config.getExposureConfiguration()
                .forDomainType(Album.class)
                .withItemExposure(((metdata, httpMethods) -> httpMethods.disable(unsupportedActions)))
                .withCollectionExposure(((metdata, httpMethods) -> httpMethods.disable(unsupportedActions)));

        config.getExposureConfiguration()
                .forDomainType(Artist.class)
                .withItemExposure(((metdata, httpMethods) -> httpMethods.disable(unsupportedActions)))
                .withCollectionExposure(((metdata, httpMethods) -> httpMethods.disable(unsupportedActions)));

        config.exposeIdsFor(Album.class);
        config.exposeIdsFor(Artist.class);
        config.exposeIdsFor(Genre.class);
        config.exposeIdsFor(Subgenre.class);

        cors.addMapping(config.getBasePath() + "/**").allowedOrigins(allowedOrigins);
    }
}
