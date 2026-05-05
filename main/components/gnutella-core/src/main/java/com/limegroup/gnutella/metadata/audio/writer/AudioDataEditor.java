package com.limegroup.gnutella.metadata.audio.writer;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.limewire.util.FileUtils;

import com.limegroup.gnutella.metadata.MetaWriter;
import com.limegroup.gnutella.metadata.audio.AudioMetaData;
import com.limegroup.gnutella.xml.LimeXMLReplyCollection.MetaDataState;

/**
 *  Handles the actual writing of the meta-information to the file. Thanks to the 
 *  JAudioTagger, all the common fields that we're concerned with implement the same
 *  interface. As a result, all the supported audio types can be written using this
 *  class. 
 */
public class AudioDataEditor implements MetaWriter {
    
    /**
     * The 7 most common meta-data audio tags can all be written using
     * this same interface. This should be overridden by a given class 
     * which wishes to write additional tags.

     * @throws FieldDataInvalidException - exception when there's a problem committing 
     *  a given tag field
     */
    protected Tag updateTag(Tag tag, AudioMetaData audioData) throws FieldDataInvalidException { 
        setField(tag, FieldKey.ALBUM, audioData.getAlbum());
        setField(tag, FieldKey.ARTIST, audioData.getArtist());
        setField(tag, FieldKey.COMMENT, audioData.getComment());
        setField(tag, FieldKey.GENRE, audioData.getGenre());
        setField(tag, FieldKey.TITLE, audioData.getTitle());
        setField(tag, FieldKey.YEAR, audioData.getYear());
        setField(tag, FieldKey.TRACK, audioData.getTrack());
        return tag;
    }

    private void setField(Tag tag, FieldKey fieldKey, String value) throws FieldDataInvalidException {
        if (value == null || value.isEmpty()) {
            try {
                tag.deleteField(fieldKey);
            } catch (KeyNotFoundException ignored) {
            }
            return;
        }

        try {
            tag.setField(fieldKey, value);
        } catch (KeyNotFoundException e) {
            throw new FieldDataInvalidException("Unsupported audio tag field: " + fieldKey, e);
        }
    }
    
    /**
     * @return true if the audio subtype was chosen properly for the file type
     */
    protected boolean isValidFileType(String fileName) {
        String fileExtension = FileUtils.getFileExtension(fileName).toLowerCase(Locale.US);
        for(String extension : getSupportedExtensions()) {
            if(fileExtension.equals(extension))
                return true;
        }
        return false;
    }
    
    /**
     * Given the audio file, return the tag from the file. If the Tag
     * doesn't already exist, return a valid tag for that audio type.
     */
    protected Tag createTag(AudioFile audioFile, AudioMetaData audioData) {
        return audioFile.getTagOrCreateAndSetDefault();
    }
    

    @Override
    public String[] getSupportedExtensions() {
        return new String[] { "fla", "flac", "m4a", "m4p", "ogg" };
    }
    
    /**
     * Performs the actual writing of the updated meta data to disk. 
     * This always writes the data to disk, it assumes that prior checks were
     * done to ensure unnecessary disk IO does not occur when no changes have
     * been made.
     * @return LimeXMLReplyCollection.NORMAL if write was successful or 
     *      a different value if write wasn't successful.
     */
    public MetaDataState commitMetaData(String fileName, AudioMetaData audioData) {
        if(!isValidFileType(fileName))
            return MetaDataState.INCORRECT_FILETYPE;
        
        File f = new File(fileName);
        FileUtils.setWriteable(f);

        AudioFile audioFile;
        
        Tag audioTag;
        
        try {
            audioFile = AudioFileIO.read(f);
            audioTag = createTag(audioFile, audioData);
            audioTag = updateTag(audioTag, audioData);
            audioFile.setTag(audioTag);
            audioFile.commit();
        } catch (CannotReadException e) {
            return MetaDataState.RW_ERROR;
        } catch (IOException e) {
            return MetaDataState.RW_ERROR;
        } catch (TagException e) {
            return MetaDataState.FAILED_ALBUM;
        } catch (ReadOnlyFileException e) {
            return MetaDataState.RW_ERROR;
        } catch (InvalidAudioFrameException e) {
            return MetaDataState.FAILED_ALBUM;
        } catch (CannotWriteException e) {
            return MetaDataState.RW_ERROR;
        }        
        return MetaDataState.NORMAL;
    }
}
