package com.optmizer.summonerwaroptimizer.service.integration.swarfarm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class FileImportService {

    public List<File> getFilesInFolder(String folderPath) {
        var folder = getFile(folderPath);

        return Stream.ofNullable(folder.listFiles())
            .flatMap(Stream::of)
            .collect(Collectors.toList());
    }

    public File getFile(String filePath) {
        return new File(filePath);
    }
}
