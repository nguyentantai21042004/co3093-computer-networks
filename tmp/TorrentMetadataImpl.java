package Peer;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class TorrentMetadataImpl implements TorrentMetadata {
    private final byte[] myInfoHash;
    @Nullable
    private final List<List<String>> myAnnounceList;
    private final String myMainAnnounce;
    private final long myCreationDate;
    private final String myComment;
    private final String myCreatedBy;
    private final String myName;
    private final List<TorrentFile> myFiles;
    private final int myPieceCount;
    private final int myPieceLength;
    private final byte[] myPiecesHashes;
    private final String myHexString;

    TorrentMetadataImpl(byte[] infoHash,
                        @Nullable List<List<String>> announceList,
                        String mainAnnounce,
                        long creationDate,
                        String comment,
                        String createdBy,
                        String name,
                        List<TorrentFile> files,
                        int pieceCount,
                        int pieceLength,
                        byte[] piecesHashes) {
        myInfoHash = infoHash;
        myAnnounceList = announceList;
        myMainAnnounce = mainAnnounce;
        myCreationDate = creationDate;
        myComment = comment;
        myCreatedBy = createdBy;
        myName = name;
        myFiles = files;
        myPieceCount = pieceCount;
        myPieceLength = pieceLength;
        myPiecesHashes = piecesHashes;
        myHexString = TorrentUtils.byteArrayToHexString(myInfoHash);
    }

    @Nullable
    @Override
    public List<List<String>> getAnnounceList() {
        return null;
    }

    @Nullable
    @Override
    public String getAnnounce() {
        return null;
    }

    @Override
    public Optional<Long> getCreationDate() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getComment() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getCreatedBy() {
        return Optional.empty();
    }

    @Override
    public int getPieceLength() {
        return 0;
    }

    @Override
    public byte[] getPiecesHashes() {
        return new byte[0];
    }

    @Override
    public boolean isPrivate() {
        return false;
    }

    @Override
    public int getPiecesCount() {
        return 0;
    }

    @Override
    public String getDirectoryName() {
        return null;
    }

    @Override
    public List<TorrentFile> getFiles() {
        return null;
    }
}
