;;;
;;;; Generic Dialog boxes
;;;
;;
;; @created   "Thu Mar 10 10:17:15 EST 2005"
;; @author    "Dominique Boucher"
;; @copyright "NuEcho Inc."
;;


(define (default-shell)
  (try-catch 
      (IWorkbenchWindow:getShell (IWorkbench:getActiveWorkbenchWindow (PlatformUI:getWorkbench)))
    (exception <java.lang.Throwable> #!null)))


(define (default-display)
  (try-catch
      (IWorkbench:getDisplay (PlatformUI:getWorkbench))
    (exception <java.lang.Throwable> #!null)))


(define (message-box title message)
  (let ((shell (default-shell)))
    (MessageDialog:openInformation shell title message)))


(define *BUTTON-WIDTH* 75)
(define *DIALOG-MARGIN-WIDTH* 5)
(define *DIALOG-MARGIN-HEIGHT* 5)
(define *BUTTON-SPACING* 5)


(define (dialog text control-builder #!optional (width 320) (height 250))
  (let* ((parent-shell  :: <org.eclipse.swt.widgets.Shell> (default-shell))
         (shell         (Shell:new parent-shell *DIALOG-STYLE*))
         (result        'cancel)
         (ok            (lambda () (set! result 'ok)     (Shell:close shell)))
         (cancel        (lambda () (set! result 'cancel) (Shell:close shell)))
         (client-area   (Composite:new shell 0))
         (button-area   (Composite:new shell 0))
         (ok-button     (new-button button-area (lambda _ (ok))
                                    text:        "Ok"
                                    layout-data: (grid-data style: '(right) width: *BUTTON-WIDTH*)))
         (cancel-button (new-button button-area (lambda _ (cancel)) 
                                    text:        "Cancel"
                                    layout-data: (grid-data style: '(right) width: *BUTTON-WIDTH*)))
         (is-ok?        (lambda (flag) (Button:setEnabled ok-button flag))))
    
    (Shell:setImage shell (Shell:getImage parent-shell))
    (Composite:setLayout     shell       (grid-layout columns:       1 
                                                      margin-width:  *DIALOG-MARGIN-WIDTH*
                                                      margin-height: *DIALOG-MARGIN-HEIGHT*))
    (Composite:setLayout     button-area (grid-layout columns:       2
                                                      margin-width:  0
                                                      margin-height: 0
                                                      hspacing:      *BUTTON-SPACING*))
    (Composite:setLayoutData button-area (grid-data style: '(right)))
    (Composite:setLayoutData client-area (grid-data style: '(fill-both)))
    
    (control-builder client-area is-ok? ok cancel)
    (when (and width height)
      (Shell:setBounds shell 0 0 width height)
      (let* ((parent-rect :: <org.eclipse.swt.graphics.Rectangle> (Shell:getBounds parent-shell))
             (new-x (+ (field parent-rect 'x) (max 0 (/ (- (field parent-rect 'width) width) 2))))
             (new-y (+ (field parent-rect 'y) (max 0 (/ (- (field parent-rect 'height) height) 2)))))
        (Shell:setBounds shell new-x new-y width height)))
  
    (Shell:setText shell (as <String> text))
    (Shell:open shell)
    (let ((display (if (eq? parent-shell #!null) 
                       (default-display)
                       (Shell:getDisplay parent-shell))))
      (while (not (Shell:isDisposed shell))
        (when (not (Display:readAndDispatch display))
          (Display:sleep display))))
    result))


(define (choose-from-list title label items)
  (let* ((selection #f)
         (control-builder
          (lambda (composite is-ok? ok cancel)
            (is-ok? #f)
            (Composite:setLayout composite (grid-layout columns: 1 margin-width: 0 margin-height: 0))
            (let ((label  (new-label composite label))
                  (viewer (new-list  composite
                                     style:        (+ *SWT.BORDER* *SWT.SINGLE* *SWT.HSCROLL*)
                                     layout-data: (grid-data style: '(fill-both))
                                     on-select:   (lambda (w indices)
                                                    (set! selection (list-ref items (car indices)))
                                                    (is-ok? #t))
                                     items:       items)))
              (List:addKeyListener
               viewer
               (object (<org.eclipse.swt.events.KeyAdapter>)
                 ((keyPressed (event :: <org.eclipse.swt.events.KeyEvent>)) :: <void>
                  (when (and (= 13 (field event 'keyCode))
                             (= 0  (field event 'stateMask))
                             selection)
                    (ok)))))))))
    
    (and (eq? 'ok (dialog title control-builder 300 250))
         selection)))

