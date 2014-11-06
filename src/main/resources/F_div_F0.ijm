ageNumber = getNumber("How many frames to average for Fzero?",6);
origTitle=getTitle();
origID=getImageID();

run("Z Project...", "start=1 stop="+ageNumber +" projection=[Average Intensity]");
fZeroID=getImageID();


//run("Image Calculator...", "image1="+origTitle+" operation=Divide image2=Fzero create 32-bit stack");
imageCalculator("Divide create 32-bit stack", origID,fZeroID);
rename(origTitle+"-FdivFzero");
setMinAndMax(0, 5);

FF0  = getImageID();

//threshold image stack
getMinAndMax(min, max);
selectImage(origID);
run("Duplicate...", "title=copy duplicate");
run("Convert to Mask", "calculate black");
run("Invert", "stack");
run("Despeckle", "stack");
maskID=getImageID();
imageCalculator("Subtract create 32-bit stack", FF0,maskID);
rename(origTitle+"-FdivFzero-threshold");
setMinAndMax(0, 5);

selectImage(maskID);
close();
selectImage(fZeroID);
close();

