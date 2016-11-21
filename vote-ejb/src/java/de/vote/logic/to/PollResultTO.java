package de.vote.logic.to;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This TO has no corresponding entity class, because it transports only the
 * plain results of a poll for presentation.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
public class PollResultTO implements Serializable {
    
    private String id;
    
    private String title, description;
    private Date startDate, endDate;
    
    private int participants, participations, idOfId;
    
    private final List<PollItemResultTO> items;
    
    public PollResultTO() {
        items = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        
        if (id != null) {
            idOfId = 0;
            for (int i = 0; i < id.length(); i++) {
                idOfId += id.charAt(i);
            }
        }
    }
    
    public int getIdOfId() {
        return idOfId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getParticipants() {
        return participants;
    }

    public void setParticipants(int participants) {
        this.participants = participants;
    }

    public int getParticipations() {
        return participations;
    }

    public void setParticipations(int participations) {
        this.participations = participations;
    }
    public List<PollItemResultTO> getItems() {
        return items;
    }
    
    public double voterParticipation() {
        return (double) (((double) participations) / ((double) participants)) * 100d;
    }
    
    public class PollItemResultTO implements Serializable {
        
        private String title;
        
        private int voidTally;
        
        private final List<PollItemOptionResultTO> options;
        
        public PollItemResultTO() {
            options = new ArrayList<>();
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getVoidTally() {
            return voidTally;
        }

        public void setVoidTally(Integer voidTally) {
            this.voidTally = voidTally == null ? 0 : voidTally;
        }

        public List<PollItemOptionResultTO> getOptions() {
            return options;
        }
        
        public double voidParticipation() {
            return (double) (((double) voidTally) / ((double) getTotalTally())) * 100d;
        }
        
        public int voidParticipationRounded() {
            return (int) voidParticipation();
        }
        
        public int getTotalTally() {
            int total = 0;
            
            for (PollItemOptionResultTO pollItemOptionResultTO : options) {
                total += pollItemOptionResultTO.getTally();
            }
            
            return total + voidTally;
        }
        
        public class PollItemOptionResultTO implements Serializable {
            
            private String shortName, description;
            
            private int tally;

            public String getShortName() {
                return shortName;
            }

            public void setShortName(String shortName) {
                this.shortName = shortName;
            }

            public String getDescription() {
                return description == null || description.isEmpty() ? "" : "- " + description;
            }

            public void setDescription(String description) {
                this.description = description;
            }
            
            public int getTally() {
                return tally;
            }

            public void setTally(Integer tally) {
                this.tally = tally == null ? 0 : tally;
            }
            
            public double participation() {
                return (double) (((double) tally) / ((double) getTotalTally())) * 100d;
            }
            
            public int participationRounded() {
                return (int) participation();
            }

        }

    }
    
}
