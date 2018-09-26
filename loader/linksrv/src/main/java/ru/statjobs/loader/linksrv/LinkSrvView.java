package ru.statjobs.loader.linksrv;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.statjobs.loader.common.dao.DownloadableLinkDao;
import ru.statjobs.loader.common.dto.DownloadableLink;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Controller
@RequestMapping("/linksrv")
public class LinkSrvView implements DownloadableLinkDao {

    @Autowired
    LinkSrvController controller;

    @Override
    @PostMapping(value = "/create")
    @ResponseBody
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean createDownloadableLink(@RequestBody DownloadableLink link) {
        return controller.createDownloadableLink(link);
    }

    @Override
    @PostMapping(value = "/createlist")
    @ResponseBody
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean createDownloadableLinks(@RequestBody List<DownloadableLink> links) {
        return controller.createDownloadableLinks(links);
    }

    @Override
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean deleteDownloadableLink(@RequestBody DownloadableLink link) {
        return controller.deleteDownloadableLink(link);
    }

    @Override
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    @Produces(MediaType.APPLICATION_JSON)
    public DownloadableLink getDownloadableLink() {
        return controller.getDownloadableLink();
    }


}
