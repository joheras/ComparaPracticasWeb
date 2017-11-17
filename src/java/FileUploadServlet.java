/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * File upload servlet example
 */
@WebServlet(name = "FileUploadServlet", urlPatterns = {"/upload"}, asyncSupported = true)
@MultipartConfig
public class FileUploadServlet extends HttpServlet {

    private final static Logger LOGGER
            = Logger.getLogger(FileUploadServlet.class.getCanonicalName());
    private static final long serialVersionUID = 7908187011456392847L;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException, InterruptedException {
        response.setContentType("text/html;charset=UTF-8");

        // Create path components to save the file
        final String path = "/home/joheras/tmp";
        final Part filePart = request.getPart("file");
        final String actividad = request.getParameter("actividad");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMddhhmmss");
        String stamp = simpleDateFormat.format(new Date());
        final String fileName = stamp + "_" + getFileName(filePart);

        OutputStream out = null;
        InputStream filecontent = null;
        //final PrintWriter writer = response.getWriter();

        try {
            out = new FileOutputStream(new File(path + File.separator
                    + fileName));
            filecontent = filePart.getInputStream();

            int read;
            final byte[] bytes = new byte[1024];

            while ((read = filecontent.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            //writer.println("New file " + fileName + " created at " + path);
            LOGGER.log(Level.INFO, "File {0} being uploaded to {1}",
                    new Object[]{fileName, path});

            if (!fileName.contains(".zip")) {
                String message = "Tienes que subir un fichero con extensión .zip.";
                request.setAttribute("message", message);
                request.getRequestDispatcher("/error.jsp").forward(request, response);
                return;
            }

            request.setAttribute("id", fileName.substring(0, fileName.lastIndexOf(".zip")) + ".txt");
            request.getRequestDispatcher("/result.jsp").forward(request, response);

            CompruebaPracticas.compruebaPractica(actividad, path + "/" + fileName);

            /*String filePath = path + "/" + fileName.substring(0, fileName.lastIndexOf(".zip")) + "/informe.txt";
            File downloadFile = new File(filePath);

            FileInputStream inStream = new FileInputStream(downloadFile);

            // if you want to use a relative path to context root:
            String relativePath = getServletContext().getRealPath("");
            System.out.println("relativePath = " + relativePath);

            // obtains ServletContext
            ServletContext context = getServletContext();

            // gets MIME type of the file
            String mimeType = context.getMimeType(filePath);
            if (mimeType == null) {
                // set to binary type if MIME mapping not found
                mimeType = "application/octet-stream";
            }
            System.out.println("MIME type: " + mimeType);

            // modifies response
            response.setContentType(mimeType);
            response.setContentLength((int) downloadFile.length());

            // forces download
            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
            response.setHeader(headerKey, headerValue);

            // obtains response's output stream
            OutputStream outStream = response.getOutputStream();

            byte[] buffer = new byte[4096];
            int bytesRead = -1;

            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }

            inStream.close();
            outStream.close();*/
        } catch (FileNotFoundException fne) {
            String message = "No has especificado un fichero.";
            request.setAttribute("message", message);
            request.getRequestDispatcher("/error.jsp").forward(request, response);

            LOGGER.log(Level.SEVERE, "Problems during file upload. Error: {0}",
                    new Object[]{fne.getMessage()});
        } finally {

            if (out != null) {
                out.close();
            }
            if (filecontent != null) {
                filecontent.close();
            }

        }
    }

    private static String getFileName(final Part part) {
        final String partHeader = part.getHeader("content-disposition");
        LOGGER.log(Level.INFO, "Part Header = {0}", partHeader);
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(
                        content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    static {
        executorService.scheduleAtFixedRate(FileUploadServlet::newEvent, 0, 2, TimeUnit.SECONDS);
    }

    private static void newEvent() {
        ArrayList clients = new ArrayList<>(queue.size());
        queue.drainTo(clients);
        clients.parallelStream().forEach((AsyncContext ac) -> {

        try {
            HttpServletResponse response = (HttpServletResponse)ac.getResponse();
            HttpServletRequest request = (HttpServletRequest)ac.getRequest();
            response.setContentType("text/html;charset=UTF-8");
            
            // Create path components to save the file
            final String path = "/home/joheras/tmp";
            final Part filePart = request.getPart("file");
            final String actividad = request.getParameter("actividad");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMddhhmmss");
            String stamp = simpleDateFormat.format(new Date());
            final String fileName = stamp + "_" + getFileName(filePart);
            
            OutputStream out = null;
            InputStream filecontent = null;
            //final PrintWriter writer = response.getWriter();
            
            try {
                out = new FileOutputStream(new File(path + File.separator
                        + fileName));
                filecontent = filePart.getInputStream();
                
                int read;
                final byte[] bytes = new byte[1024];
                
                while ((read = filecontent.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                //writer.println("New file " + fileName + " created at " + path);
                LOGGER.log(Level.INFO, "File {0} being uploaded to {1}",
                        new Object[]{fileName, path});
                
                if (!fileName.contains(".zip")) {
                    String message = "Tienes que subir un fichero con extensión .zip.";
                    request.setAttribute("message", message);
                    request.getRequestDispatcher("/error.jsp").forward(request, response);
                    return;
                }
                
                request.setAttribute("id", fileName.substring(0, fileName.lastIndexOf(".zip")) + ".txt");
                request.getRequestDispatcher("/result.jsp").forward(request, response);
                
                CompruebaPracticas.compruebaPractica(actividad, path + "/" + fileName);
                
            } catch (FileNotFoundException fne) {
                String message = "No has especificado un fichero.";
                request.setAttribute("message", message);
                request.getRequestDispatcher("/error.jsp").forward(request, response);
                
                LOGGER.log(Level.SEVERE, "Problems during file upload. Error: {0}",
                        new Object[]{fne.getMessage()});
            } finally {
                
                if (out != null) {
                    out.close();
                }
                if (filecontent != null) {
                    filecontent.close();
                }
                
            }
            ac.complete();
        } catch (IOException ex) {
                Logger.getLogger(FileUploadServlet.class.getName()).log(Level.SEVERE, null, ex);
        }   catch (ServletException ex) {
                Logger.getLogger(FileUploadServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private static final BlockingQueue queue = new ArrayBlockingQueue<>(20000);

    public static void addToWaitingList(AsyncContext c) {
        queue.add(c);
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        addToWaitingList(request.startAsync());
        /*try {
            processRequest(request, response);
        } catch (InterruptedException ex) {
            Logger.getLogger(FileUploadServlet.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (InterruptedException ex) {
            Logger.getLogger(FileUploadServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Servlet that uploads files to a user-defined destination";
    }
}
