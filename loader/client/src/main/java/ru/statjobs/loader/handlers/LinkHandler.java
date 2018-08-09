package ru.statjobs.loader.handlers;


import ru.statjobs.loader.common.dto.DownloadableLink;

public interface LinkHandler {

    void process(DownloadableLink link);

}
