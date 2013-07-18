plotpoints = read.table(file, header=FALSE, sep="\t");

queries = as.character(plotpoints$V3);

uniqueQueries = unique(queries);


     

for(i in 1:length(uniqueQueries)) {
  query = uniqueQueries[i];



  
  my.subset = subset(plotpoints, V3==query);

  pngName = paste(gsub(':', '_', query), ".png", sep="");
  png(file=pngName, width = 700, height = 480);

  
  x = as.numeric(as.vector(my.subset[,1]));
  y = as.numeric(as.vector(my.subset[,2]));


  plot(x,
       type = "n",
       xlab = "Seconds",
       xlim = c(min(x), max(x)),
       ylab = "Duration",
       ylim = c(min(y), max(y)),
       );

  points(x,y, col=rainbow(i));

  dev.off()  
}


    

