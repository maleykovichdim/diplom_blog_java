package blog.service;

import blog.api.response.SettingResponseRequest;
import blog.model.GlobalSetting;
import blog.model.repository.GlobalSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    public static final String TRUE = "true";
    public static final String FALSE = "false";
    private final GlobalSettingRepository globalSettingRepository;

    @Autowired
    public SettingsService(GlobalSettingRepository globalSettingRepository) {
        this.globalSettingRepository = globalSettingRepository;
    }

    public SettingResponseRequest getGlobalSettings()
    {
        SettingResponseRequest settingResponseRequest = new SettingResponseRequest();
        settingResponseRequest.setMultiuserMode(
                globalSettingRepository.findByCode("MULTIUSER_MODE").get().getValue().equals(TRUE));
        settingResponseRequest.setStatisticsIsPublic(
                globalSettingRepository.findByCode("POST_PREMODERATION").get().getValue().equals(TRUE));
        settingResponseRequest.setPostPremoderation(
                globalSettingRepository.findByCode("STATISTICS_IS_PUBLIC").get().getValue().equals(TRUE));
        return settingResponseRequest;
    }

    public void setGlobalSettings(SettingResponseRequest requestBody)
    {
        GlobalSetting multiUser = globalSettingRepository.findByCode("MULTIUSER_MODE").get();
        GlobalSetting postPremoderation = globalSettingRepository.findByCode("POST_PREMODERATION").get();
        GlobalSetting statisticIsPublic = globalSettingRepository.findByCode("STATISTICS_IS_PUBLIC").get();
        multiUser.setValue(requestBody.isMultiuserMode() ? TRUE : FALSE);
        postPremoderation.setValue(requestBody.isPostPremoderation()? TRUE : FALSE);
        statisticIsPublic.setValue(requestBody.isStatisticsIsPublic()? TRUE : FALSE);
        globalSettingRepository.save(multiUser);
        globalSettingRepository.save(postPremoderation);
        globalSettingRepository.save(statisticIsPublic);
    }




}

